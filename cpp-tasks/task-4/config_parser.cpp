#include "config_parser.h"
#include <algorithm>
#include <cctype>
#include <fstream>
#include <sstream>
#include <stdexcept>

ConfigParser::ConfigParser(int num_input_files)
    : num_input_files(num_input_files)
{ }

std::vector<ConverterConfig> ConfigParser::parse(const std::string& filename)
{
    std::vector<ConverterConfig> configs;

    std::ifstream file(filename);
    if (!file.is_open()) {
        throw std::runtime_error("Cant open config: "
                                 + filename);
    }

    std::string line;
    int lineNum = 0;

    while (std::getline(file, line)) {
        lineNum++;

        line = Trim(line);
        if (line.empty() || IsComment(line)) {
            continue;
        }

        std::vector<std::string> tokens = split(line);
        if (tokens.empty()) {
            continue;
        }

        ConverterConfig config;
        config.name = tokens[0];
        config.secondary_stream_index = -1;


        for (size_t i = 1; i < tokens.size(); i++) {
            config.args.push_back(tokens[i]);
        }

        if (!ValidateConverter(
                config.name, config.args, config.secondary_stream_index))
        {
            std::ostringstream oss;
            oss << "line " << lineNum << ":error in converter '"
                << config.name << "' with arguments:";
            for (const auto& arg : config.args) {
                oss << " " << arg;
            }
            throw std::runtime_error(oss.str());
        }

        configs.push_back(config);
    }

    return configs;
}

std::vector<std::unique_ptr<Converter>> ConfigParser::CreateConverters(
    const std::vector<ConverterConfig>& configs,
    const std::vector<std::vector<int16_t>>& secondary_streams)
{

    std::vector<std::unique_ptr<Converter>> converters;

    for (const auto& config : configs) {
        auto converter = ConverterFactory::GetInstance().CreateConverter(
            config.name, config.args);


        if (converter->RequiresSecondaryStream()) {
            if (config.secondary_stream_index >= 0
                && config.secondary_stream_index
                       < static_cast<int>(secondary_streams.size()))
            {
                converter->SetSecondaryStream(
                    &secondary_streams[config.secondary_stream_index]);
            }
        }

        converters.push_back(std::move(converter));
    }

    return converters;
}

std::string ConfigParser::Trim(const std::string& str)
{
    size_t first = str.find_first_not_of(" \t");
    if (first == std::string::npos)
        return "";

    size_t last = str.find_last_not_of(" \t");
    return str.substr(first, last - first + 1);
}

std::vector<std::string> ConfigParser::split(const std::string& str)
{
    std::vector<std::string> tokens;
    std::istringstream iss(str);
    std::string token;

    while (iss >> token) {
        tokens.push_back(token);
    }

    return tokens;
}

bool ConfigParser::IsComment(const std::string& line)
{
    return !line.empty() && line[0] == '#';
}

bool ConfigParser::ParseStreamReference(const std::string& token,
                                        int& stream_index)
{
    if (token.empty() || token[0] != '$') {
        return false;
    }

    try {
        stream_index = std::stoi(token.substr(1)) - 1; 
        return stream_index >= 0 && stream_index < num_input_files;
    } catch (...) {
        return false;
    }
}

bool ConfigParser::ValidateConverter(const std::string& name,
                                     const std::vector<std::string>& args,
                                     int& secondary_stream_index)
{
    secondary_stream_index = -1;

    if (name == "mute") {
        if (args.size() != 2)
            return false;

        try {
            double start = std::stod(args[0]);
            double end = std::stod(args[1]);
            return start >= 0 && end >= 0 && start <= end;
        } catch (...) {
            return false;
        }
    }
    else if (name == "mix") {
        if (args.size() < 1 || args.size() > 2)
            return false;

        if (!ParseStreamReference(args[0], secondary_stream_index)) {
            return false;
        }

        if (args.size() == 2) {
            try {
                double offset = std::stod(args[1]);
                return offset >= 0;
            } catch (...) {
                return false;
            }
        }

        return true;
    }

    else if (name == "echo") {
        if (args.size() != 2)
            return false;

        try {
            double delay = std::stod(args[0]);
            double decay = std::stod(args[1]);
            return delay > 0 && decay >= 0 && decay <= 1;
        } catch (...) {
            return false;
        }
    }

    return false;
}