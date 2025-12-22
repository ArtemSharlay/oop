#include "wav.h"
#include <algorithm>
#include <cstring>
#include <iostream>
#include <vector>

WavFile::WavFile()
    : data_start_pos(0)
{
    memset(&header, 0, sizeof(Header));
}

WavFile::~WavFile()
{
    Close();
}

bool WavFile::OpenForRead(const std::string& filename)
{
    file.open(filename, std::ios::binary | std::ios::in);
    if (!file.is_open()) {
        std::cerr << "cant open for reading: " << filename
                  << std::endl;
        return false;
    }
    return ReadHeader();
}

bool WavFile::OpenForWrite(const std::string& filename)
{
    file.open(filename, std::ios::binary | std::ios::out | std::ios::trunc);
    if (!file.is_open()) {
        std::cerr << "cant open for writing: " << filename
                  << std::endl;
        return false;
    }
    return true;
}

void WavFile::Close()
{
    if (file.is_open()) {
        file.close();
    }
}

bool WavFile::ReadHeader()
{
    file.read(reinterpret_cast<char*>(&header.riff), 4);
    file.read(reinterpret_cast<char*>(&header.chunk_size), 4);
    file.read(reinterpret_cast<char*>(&header.wave), 4);

    if (!file) {
        std::cerr << "error cant read basic title WAV"
                  << std::endl;
        return false;
    }

    if (memcmp(header.riff, "RIFF", 4) != 0
        || memcmp(header.wave, "WAVE", 4) != 0)
    {
        std::cerr << "error wrong format (not RIFF/WAVE)"
                  << std::endl;
        return false;
    }


    bool fmt_found = false;
    bool data_found = false;
    char chunk_id[4];
    uint32_t chunk_size;

    while (!data_found && file) {
        file.read(chunk_id, 4);
        file.read(reinterpret_cast<char*>(&chunk_size), 4);

        if (!file)
            break;

        if (memcmp(chunk_id, "fmt ", 4) == 0) {
            fmt_found = true;

            file.read(reinterpret_cast<char*>(&header.audio_format), 2);
            file.read(reinterpret_cast<char*>(&header.num_channels), 2);
            file.read(reinterpret_cast<char*>(&header.sample_rate), 4);
            file.read(reinterpret_cast<char*>(&header.byte_rate), 4);
            file.read(reinterpret_cast<char*>(&header.block_align), 2);
            file.read(reinterpret_cast<char*>(&header.bits_per_sample), 2);

            if (chunk_size > 16) {
                file.seekg(chunk_size - 16, std::ios::cur);
            }
        }
        else if (memcmp(chunk_id, "data", 4) == 0) {
            data_found = true;
            memcpy(header.data, "data", 4);
            header.subchunk_2_size = chunk_size;
            data_start_pos = file.tellg();


            file.seekg(0, std::ios::end);
            size_t file_size = file.tellg();
            file.seekg(data_start_pos, std::ios::beg);

            size_t expected_data_end = data_start_pos + chunk_size;
            if (expected_data_end > file_size) {
                std::cerr << "Warning chunc size too big ("
                          << chunk_size << ") "
                          << std::endl;
                header.subchunk_2_size = file_size - data_start_pos;
            }
        }
        else {
            file.seekg(chunk_size, std::ios::cur);
        }
    }

    if (!fmt_found) {
        std::cerr << "error no fmt chunk" << std::endl;
        return false;
    }

    if (!data_found) {
        std::cerr << "error no data chunk" << std::endl;
        return false;
    }

    memcpy(header.fmt, "fmt ", 4);
    header.subchunk_1_size = 16; 

    return IsSupportedFormat();
}

bool WavFile::WriteHeader(uint32_t data_size)
{
    uint32_t file_size = 36 + data_size; 


    memcpy(header.riff, "RIFF", 4);
    header.chunk_size = file_size - 8;
    memcpy(header.wave, "WAVE", 4);
    memcpy(header.fmt, "fmt ", 4);
    header.subchunk_1_size = 16;
    header.audio_format = 1;
    header.num_channels = REQUIRED_CHANNELS;
    header.sample_rate = REQUIRED_SAMPLE_RATE;
    header.bits_per_sample = REQUIRED_BITS_PER_SAMPLE;
    header.byte_rate =
        REQUIRED_SAMPLE_RATE * REQUIRED_CHANNELS * REQUIRED_BITS_PER_SAMPLE / 8;
    header.block_align = REQUIRED_CHANNELS * REQUIRED_BITS_PER_SAMPLE / 8;
    memcpy(header.data, "data", 4);
    header.subchunk_2_size = data_size;

    file.seekp(0);
    file.write(reinterpret_cast<const char*>(&header), sizeof(Header));

    return file.good();
}

size_t WavFile::ReadSamplesWithCount(int16_t* buffer, size_t count)
{
    if (count == 0)
        return 0;

   
    size_t current_pos = file.tellg();
    size_t data_end = data_start_pos + header.subchunk_2_size;

    if (current_pos >= data_end) {
        return 0;
    }

    size_t max_samples_to_read = (data_end - current_pos) / sizeof(int16_t);
    size_t samples_to_read = std::min(count, max_samples_to_read);

    if (samples_to_read == 0) {
        return 0;
    }

    file.read(reinterpret_cast<char*>(buffer), samples_to_read * sizeof(int16_t));
    size_t bytes_read = file.gcount();
    size_t samples_read = bytes_read / sizeof(int16_t);

    return samples_read;
}

bool WavFile::ReadSamples(int16_t* buffer, size_t count)
{
    size_t samples_read = ReadSamplesWithCount(buffer, count);

    if (samples_read < count) {
        std::fill(buffer + samples_read, buffer + count, 0);
    }

    return samples_read > 0;
}

bool WavFile::WriteSamples(const int16_t* buffer, size_t count)
{
    file.write(reinterpret_cast<const char*>(buffer), count * sizeof(int16_t));
    return file.good();
}

bool WavFile::IsSupportedFormat() const
{
    if (header.audio_format != 1) {
        std::cerr << "Error only PCM format" << std::endl;
        return false;
    }

    if (header.num_channels != REQUIRED_CHANNELS) {
        std::cerr << "error can use only mono "
                  << std::endl;
        return false;
    }

    if (header.sample_rate != REQUIRED_SAMPLE_RATE) {
        std::cerr << "Error can use only frequancy 44100 HZ"
                  << std::endl;
        return false;
    }

    if (header.bits_per_sample != REQUIRED_BITS_PER_SAMPLE) {
        std::cerr << "Error can use only 16 bit on sample"
                  << std::endl;
        return false;
    }

    return true;
}

uint32_t WavFile::GetNumSamples() const
{
    if (header.bits_per_sample == 0 || header.num_channels == 0) {
        return 0;
    }
    return header.subchunk_2_size / (header.bits_per_sample / 8)
           / header.num_channels;
}