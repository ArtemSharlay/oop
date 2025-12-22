#ifndef PIPELINE_H
#define PIPELINE_H

#include "converters.h"
#include "wav.h"
#include <memory>
#include <string>
#include <vector>

class ProcessingPipeline
{
public:
    ProcessingPipeline();

    void AddConverter(std::unique_ptr<Converter> converter);
    void SetSecondaryStream(int index, const std::vector<int16_t>& stream);

    void Process(WavFile& input, WavFile& output);

private:
    std::vector<std::unique_ptr<Converter>> converters;
    static const size_t BUFFER_SIZE = 4096;
};

#endif