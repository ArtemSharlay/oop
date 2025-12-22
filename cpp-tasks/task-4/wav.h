#ifndef WAV_H
#define WAV_H

#include <cstdint>
#include <fstream>
#include <string>
#include <vector>

class WavFile
{
public:
    struct Header
    {
        char riff[4];
        uint32_t chunk_size;
        char wave[4];
        char fmt[4];
        uint32_t subchunk_1_size;
        uint16_t audio_format;
        uint16_t num_channels;
        uint32_t sample_rate;
        uint32_t byte_rate;
        uint16_t block_align;
        uint16_t bits_per_sample;
        char data[4];
        uint32_t subchunk_2_size;
    };

    WavFile();
    ~WavFile();

    bool OpenForRead(const std::string& filename);
    bool OpenForWrite(const std::string& filename);
    void Close();

    bool ReadHeader();
    bool WriteHeader(uint32_t data_size);

    bool ReadSamples(int16_t* buffer, size_t count);
    size_t ReadSamplesWithCount(int16_t* buffer, size_t count);
    bool WriteSamples(const int16_t* buffer, size_t count);

    bool IsOpen() const
    {
        return file.is_open();
    }
    bool IsSupportedFormat() const;

    uint32_t GetSampleRate() const
    {
        return header.sample_rate;
    }
    uint32_t GetNumSamples() const;
    uint32_t GetDataSize() const
    {
        return header.subchunk_2_size;
    }

    static const uint32_t REQUIRED_SAMPLE_RATE = 44100;
    static const uint16_t REQUIRED_CHANNELS = 1;
    static const uint16_t REQUIRED_BITS_PER_SAMPLE = 16;

private:
    std::fstream file;
    Header header;
    size_t data_start_pos;
};

#endif