#include "pipeline.h"
#include <algorithm>
#include <iostream>

ProcessingPipeline::ProcessingPipeline()
{ }

void ProcessingPipeline::AddConverter(std::unique_ptr<Converter> converter)
{
    converters.push_back(std::move(converter));
}

void ProcessingPipeline::SetSecondaryStream(int index,
                                            const std::vector<int16_t>& stream)
{

    for (auto& converter : converters) {
        if (converter->RequiresSecondaryStream()) {
            converter->SetSecondaryStream(&stream);
        }
    }
}

void ProcessingPipeline::Process(WavFile& input, WavFile& output)
{

    uint32_t total_samples = input.GetNumSamples();
    if (total_samples == 0) {
        throw std::runtime_error("input empty");
    }


    uint32_t sample_rate = input.GetSampleRate();
    for (auto& converter : converters) {
        converter->Prepare(sample_rate);
        converter->Reset();
    }

    std::vector<int16_t> buffer1(BUFFER_SIZE);
    std::vector<int16_t> buffer2(BUFFER_SIZE);

    size_t samples_processed = 0;

    while (samples_processed < total_samples) {
        size_t samples_to_read =
            std::min(BUFFER_SIZE, total_samples - samples_processed);

        size_t actual_read =
            input.ReadSamplesWithCount(buffer1.data(), samples_to_read);
        if (actual_read == 0) {
            break; 
        }

        int16_t* src = buffer1.data();
        int16_t* dst = buffer2.data();

        for (size_t i = 0; i < converters.size(); i++) {
            converters[i]->Process(src, dst, actual_read);

            std::swap(src, dst);
        }


        if (!output.WriteSamples(src, actual_read)) {
            throw std::runtime_error("error in write");
        }

        samples_processed += actual_read;

        if (total_samples > 0 && (samples_processed % (total_samples / 10)) == 0) {
            int percent = (samples_processed * 100) / total_samples;
            std::cout << "progress: " << percent << "%" << std::endl;
        }
    }


    uint32_t data_size = samples_processed * sizeof(int16_t);
    if (!output.WriteHeader(data_size)) {
        throw std::runtime_error("error output title");
    }

    std::cout << "The end. samples processed: " << samples_processed
              << " (" << (static_cast<double>(samples_processed) / sample_rate)
              << " sec)" << std::endl;
}