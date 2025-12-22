#include "config_parser.h"
#include "pipeline.h"
#include "wav.h"
#include <iostream>
#include <memory>
#include <stdexcept>
#include <string>
#include <vector>

void PrintUsage()
{
    std::cout << "Использование:\n";
    std::cout << "  sound_processor -c <config.txt> <output.wav> <input1.wav> "
                 "[<input2.wav> ...]\n";
    std::cout << "  sound_processor -h | --help\n";
}

void PrintHelp()
{
    std::cout << ConverterFactory::GetInstance().GetHelpText();
}

void LoadSecondaryStream(const std::string& filename,
                         std::vector<int16_t>& stream)
{
    WavFile wav;
    if (!wav.OpenForRead(filename)) {
        throw std::runtime_error("cant open secondary file: "
                                 + filename);
    }

    if (!wav.IsSupportedFormat()) {
        throw std::runtime_error("wrong format in secondary file: "
                                 + filename);
    }

    uint32_t num_samples = wav.GetNumSamples();
    if (num_samples == 0) {
        throw std::runtime_error("secondary file empty: " + filename);
    }

    stream.resize(num_samples);
    wav.ReadSamples(stream.data(), num_samples);
    wav.Close();

    std::cout << "loaded second stream: " << filename << " (" << num_samples
              << " samples)" << std::endl;
}

int main(int argc, char* argv[])
{
    try {

        if (argc < 2) {
            PrintUsage();
            return 1;
        }

        std::string option = argv[1];

        if (option == "-h" || option == "--help") {
            PrintHelp();
            return 0;
        }

        if (option != "-c" || argc < 5) {
            std::cerr << "error wrong arguments\n";
            PrintUsage();
            return 1;
        }

        std::string config_file = argv[2];
        std::string output_file = argv[3];

        std::vector<std::string> input_files;
        for (int i = 4; i < argc; i++) {
            input_files.push_back(argv[i]);
        }

        if (input_files.empty()) {
            std::cerr
                << "error need at least 1 file\n";
            return 1;
        }

        std::cout << "starting" << std::endl;
        std::cout << "config: " << config_file << std::endl;
        std::cout << "output file: " << output_file << std::endl;
        std::cout << "input files: " << input_files.size() << std::endl;

        ConfigParser parser(static_cast<int>(input_files.size()));
        auto converter_configs = parser.parse(config_file);

        if (converter_configs.empty()) {
            std::cout << "config empty "
                         
                      << std::endl;
        }

        std::cout << "found converters: " << converter_configs.size()
                  << std::endl;

        std::vector<std::vector<int16_t>> secondary_streams(input_files.size());
        bool has_secondary_streams = false;

        for (const auto& config : converter_configs) {
            if (config.secondary_stream_index >= 0) {
                int idx = config.secondary_stream_index;
                if (idx < input_files.size() && secondary_streams[idx].empty()) {
                    try {
                        LoadSecondaryStream(input_files[idx],
                                            secondary_streams[idx]);
                        has_secondary_streams = true;
                    } catch (const std::exception& e) {
                        std::cerr
                            << "error loading 2 stream: " << e.what()
                            << std::endl;
                        return 2;
                    }
                }
            }
        }

        auto converters =
            parser.CreateConverters(converter_configs, secondary_streams);

        WavFile input_wav;
        if (!input_wav.OpenForRead(input_files[0])) {
            std::cerr << "error cant open input: "
                      << input_files[0] << std::endl;
            return 2;
        }

        if (!input_wav.IsSupportedFormat()) {
            std::cerr << "error wrong format"
                      << std::endl;
            return 3;
        }

        uint32_t input_samples = input_wav.GetNumSamples();
        if (input_samples == 0) {
            std::cerr << "error input file empty" << std::endl;
            return 2;
        }

        std::cout << "main input: " << input_files[0] << std::endl;
        std::cout << "  length: "
                  << (static_cast<double>(input_samples) / 44100.0) << " sec"
                  << std::endl;
        std::cout << "  samples: " << input_samples << std::endl;

        WavFile output_wav;
        if (!output_wav.OpenForWrite(output_file)) {
            std::cerr << "error couldnt create output: "
                      << output_file << std::endl;
            return 2;
        }


        ProcessingPipeline pipeline;
        for (auto& converter : converters) {
            pipeline.AddConverter(std::move(converter));
        }

        pipeline.Process(input_wav, output_wav);


        input_wav.Close();
        output_wav.Close();

        std::cout << "Success. saved in: "
                  << output_file << std::endl;

    } catch (const std::exception& e) {
        std::cerr << "error: " << e.what() << std::endl;
        return 5;
    } catch (...) {
        std::cerr << "unknown error" << std::endl;
        return 5;
    }

    return 0;
}