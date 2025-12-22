#ifndef CONFIG_PARSER_H
#define CONFIG_PARSER_H

#include "converters.h"
#include <memory>
#include <string>
#include <vector>

struct ConverterConfig
{
    std::string name;
    std::vector<std::string> args;
    int secondary_stream_index;
};

class ConfigParser
{
public:
    ConfigParser(int num_input_files);

    std::vector<ConverterConfig> parse(const std::string& filename);
    std::vector<std::unique_ptr<Converter>>
    CreateConverters(const std::vector<ConverterConfig>& configs,
                     const std::vector<std::vector<int16_t>>& secondary_streams);

private:
    int num_input_files;

    std::string Trim(const std::string& str);
    std::vector<std::string> split(const std::string& str);
    bool IsComment(const std::string& line);
    bool ParseStreamReference(const std::string& token, int& stream_index);
    bool ValidateConverter(const std::string& name,
                           const std::vector<std::string>& args,
                           int& secondary_stream_index);
};

#endif