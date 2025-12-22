#include "converters.h"
#include <cmath>
#include <iostream>
#include <sstream>
#include <stdexcept>

int16_t Clamp16(int32_t value)
{
    if (value > 32767)
        return 32767;
    if (value < -32768)
        return -32768;
    return static_cast<int16_t>(value);
}


MuteConverter::MuteConverter(double start_time, double end_time)
    : start_time(start_time)
    , end_time(end_time)
    , start_sample(0)
    , end_sample(0)
    , current_sample(0)
{ }

void MuteConverter::Prepare(size_t sample_rate)
{
    start_sample = static_cast<size_t>(std::round(start_time * sample_rate));
    end_sample = static_cast<size_t>(std::round(end_time * sample_rate));
    current_sample = 0;
}

void MuteConverter::Process(const int16_t* input,
                            int16_t* output,
                            size_t num_samples)
{
    for (size_t i = 0; i < num_samples; i++) {
        if (current_sample >= start_sample && current_sample < end_sample) {
            output[i] = 0; 
        }
        else {
            output[i] = input[i];
        }
        current_sample++;
    }
}


MixConverter::MixConverter(int stream_index, double offset)
    : stream_index(stream_index)
    , offset(offset)
    , offset_samples(0)
    , secondary_stream(nullptr)
    , current_sample(0)
{ }

void MixConverter::Prepare(size_t sample_rate)
{
    offset_samples = static_cast<size_t>(std::round(offset * sample_rate));
    current_sample = 0;
}

void MixConverter::SetSecondaryStream(const std::vector<int16_t>* stream)
{
    secondary_stream = stream;
}

void MixConverter::Process(const int16_t* input,
                           int16_t* output,
                           size_t num_samples)
{
    if (!secondary_stream) {
        std::copy(input, input + num_samples, output);
        current_sample += num_samples;
        return;
    }

    for (size_t i = 0; i < num_samples; i++) {
        int32_t mixed = input[i];

        if (current_sample >= offset_samples) {
            size_t secondaryIdx = current_sample - offset_samples;
            if (secondaryIdx < secondary_stream->size()) {
                mixed += (*secondary_stream)[secondaryIdx];
                mixed = mixed / 2;
            }
        }

        output[i] = Clamp16(mixed);
        current_sample++;
    }
}


EchoConverter::EchoConverter(double delay, double decay)
    : delay(delay)
    , decay(decay)
    , delay_samples(0)
    , buffer_pos(0)
{ }

void EchoConverter::Prepare(size_t sample_rate)
{
    delay_samples = static_cast<size_t>(std::round(delay * sample_rate));
    if (delay_samples < 1)
        delay_samples = 1;
    buffer.resize(delay_samples, 0);
    buffer_pos = 0;
}

void EchoConverter::Reset()
{
    std::fill(buffer.begin(), buffer.end(), 0);
    buffer_pos = 0;
}

void EchoConverter::Process(const int16_t* input,
                            int16_t* output,
                            size_t num_samples)
{
    for (size_t i = 0; i < num_samples; i++) {
        int32_t echoValue =
            static_cast<int32_t>(std::round(buffer[buffer_pos] * decay));
        int32_t result = input[i] + echoValue;

        output[i] = Clamp16(result);

        buffer[buffer_pos] = output[i];
        buffer_pos = (buffer_pos + 1) % delay_samples;
    }
}


ConverterFactory& ConverterFactory::GetInstance()
{
    static ConverterFactory factory;
    return factory;
}

ConverterFactory::ConverterFactory()
{
    RegisterDefaults();
}

void ConverterFactory::RegisterDefaults()
{
    RegisterConverter(
        "mute",
        [](const std::vector<std::string>& args) -> std::unique_ptr<Converter> {
            if (args.size() != 2)
                throw std::runtime_error("mute needs 2 arguments");
            double start = std::stod(args[0]);
            double end = std::stod(args[1]);
            return std::make_unique<MuteConverter>(start, end);
        },
        "mute [start, end) sec",
        "mute <start> <end>");

    RegisterConverter(
        "mix",
        [](const std::vector<std::string>& args) -> std::unique_ptr<Converter> {
            if (args.size() < 1 || args.size() > 2)
                throw std::runtime_error("mix needs 1 or 2 arguments");

            double offset = 0.0;
            if (args.size() == 2) {
                offset = std::stod(args[1]);
            }

            return std::make_unique<MixConverter>(
                0, offset); 
        },
        "mix from offset sec",
        "mix $<n> [offset]");


    RegisterConverter(
        "echo",
        [](const std::vector<std::string>& args) -> std::unique_ptr<Converter> {
            if (args.size() != 2)
                throw std::runtime_error("echo needs 2 arguments");
            double delay = std::stod(args[0]);
            double decay = std::stod(args[1]);
            return std::make_unique<EchoConverter>(delay, decay);
        },
        "echo effect with delay ,decay",
        "echo <delay> <decay>");
}

void ConverterFactory::RegisterConverter(const std::string& name,
                                         Creator creator,
                                         const std::string& description,
                                         const std::string& syntax)
{
    converters[name] = { creator, description, syntax };
}

std::unique_ptr<Converter>
ConverterFactory::CreateConverter(const std::string& name,
                                  const std::vector<std::string>& args)
{

    auto it = converters.find(name);
    if (it == converters.end()) {
        throw std::runtime_error("unknown converter: " + name);
    }

    return it->second.creator(args);
}

std::vector<std::string> ConverterFactory::GetAvailableConverters() const
{
    std::vector<std::string> result;
    for (const auto& pair : converters) {
        result.push_back(pair.first);
    }
    return result;
}

std::string ConverterFactory::GetConverterInfo(const std::string& name) const
{
    auto it = converters.find(name);
    if (it == converters.end()) {
        return "";
    }

    std::ostringstream oss;
    oss << "  " << it->second.syntax << "\n    " << it->second.description;
    return oss.str();
}

std::string ConverterFactory::GetHelpText() const
{
    std::ostringstream oss;

    oss << "Sound Processor \n\n";
    oss << "using:\n";
    oss << "  sound_processor -c <config.txt> <output.wav> <input1.wav> "
           "[<input2.wav> ...]\n";
    oss << "  sound_processor -h | --help\n\n";

    oss << "Supported format WAV:\n";
    oss << "  - format: RIFF/WAVE, PCM\n";
    oss << "  - channels: 1 \n";
    oss << "  - frequency: 44100 HZ\n";
    oss << "  - bits: 16 бит \n\n";

    oss << "available converters :\n";
    for (const auto& pair : converters) {
        oss << GetConverterInfo(pair.first) << "\n\n";
    }

    oss << "Links to streams:\n";
    oss << "  $n - the link to the input file, where n is the file number\n";
    oss << "  Example: $1 is the first input file, $2 is the second, etc.\n\n";

    oss << "Units of measurement:\n";
    oss << "  Time: seconds (real numbers)\n";
    oss << "  Coefficients: real numbers (e.g. 0.5, 1.5, 2.0)\n";

    return oss.str();
}