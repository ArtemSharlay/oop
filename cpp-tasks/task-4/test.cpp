#include "config_parser.h"
#include "converters.h"
#include "wav.h"
#include <filesystem>
#include <fstream>
#include <gtest/gtest.h>
#include <vector>

namespace fs = std::filesystem;


class SoundProcessorTest : public ::testing::Test
{
protected:
    void SetUp() override
    {
        test_dir = fs::temp_directory_path() / "sound_processor_test";
        fs::create_directories(test_dir);
    }

    void TearDown() override
    {
        fs::remove_all(test_dir);
    }

    void CreateTestWavFile(const fs::path& path,
                           const std::vector<int16_t>& samples)
    {
        WavFile wav;
        wav.OpenForWrite(path.string());
        wav.WriteHeader(samples.size() * sizeof(int16_t));
        wav.WriteSamples(samples.data(), samples.size());
        wav.Close();
    }

    std::vector<int16_t> GenerateTestSamples(size_t count)
    {
        std::vector<int16_t> samples(count);
        for (size_t i = 0; i < count; i++) {
            samples[i] = static_cast<int16_t>((i % 100) * 100 - 5000);
        }
        return samples;
    }

    fs::path test_dir;
};


TEST_F(SoundProcessorTest, WavRoundTrip)
{

    auto samples = GenerateTestSamples(100);
    auto filepath = test_dir / "test.wav";

    {
        WavFile wav;
        ASSERT_TRUE(wav.OpenForWrite(filepath.string()));
        ASSERT_TRUE(wav.WriteHeader(samples.size() * sizeof(int16_t)));
        ASSERT_TRUE(wav.WriteSamples(samples.data(), samples.size()));
        wav.Close();
    }


    std::vector<int16_t> readSamples(samples.size());
    {
        WavFile wav;
        ASSERT_TRUE(wav.OpenForRead(filepath.string()));
        ASSERT_TRUE(wav.IsSupportedFormat());
        EXPECT_EQ(wav.GetNumSamples(), samples.size());

        size_t totalRead = 0;
        const size_t CHUNK_SIZE = 20;
        while (totalRead < samples.size()) {
            size_t toRead = std::min(CHUNK_SIZE, samples.size() - totalRead);
            size_t read =
                wav.ReadSamplesWithCount(&readSamples[totalRead], toRead);
            EXPECT_EQ(read, toRead);
            totalRead += read;
        }
        wav.Close();
    }


    for (size_t i = 0; i < samples.size(); i++) {
        EXPECT_EQ(readSamples[i], samples[i]);
    }
}

TEST_F(SoundProcessorTest, WavFormatValidation)
{

    auto filepath = test_dir / "bad.wav";


    WavFile::Header header = {};
    memcpy(header.riff, "RIFF", 4);
    header.chunk_size = 36;
    memcpy(header.wave, "WAVE", 4);
    memcpy(header.fmt, "fmt ", 4);
    header.subchunk_1_size = 16;
    header.audio_format = 3; // Не PCM!
    header.num_channels = 2; // Не моно!
    header.sample_rate = 22050; // Не 44100!
    header.bits_per_sample = 24; // Не 16 бит!
    memcpy(header.data, "data", 4);
    header.subchunk_2_size = 0;

    std::ofstream file(filepath, std::ios::binary);
    file.write(reinterpret_cast<const char*>(&header), sizeof(header));
    file.close();

    WavFile wav;
    EXPECT_FALSE(wav.IsSupportedFormat());
}


TEST_F(SoundProcessorTest, MuteConverterBasic)
{
    MuteConverter mute(0.0, 0.0005);
    mute.Prepare(44100);

    std::vector<int16_t> input(100, 1000);
    std::vector<int16_t> output(input.size());

    mute.Process(input.data(), output.data(), input.size());


    for (int i = 0; i < 22; i++) {
        EXPECT_EQ(output[i], 0);
    }


    for (size_t i = 22; i < input.size(); i++) {
        EXPECT_EQ(output[i], 1000);
    }
}

TEST_F(SoundProcessorTest, MixConverterBasic)
{
    std::vector<int16_t> secondary = { 500, 1000, 1500 };
    MixConverter mix(0, 0.0);
    mix.Prepare(44100);
    mix.SetSecondaryStream(&secondary);

    std::vector<int16_t> input = { 1000, 2000, 3000, 4000, 5000 };
    std::vector<int16_t> output(input.size());

    mix.Process(input.data(), output.data(), input.size());


    EXPECT_EQ(output[0], 750); 
    EXPECT_EQ(output[1], 1500); 
    EXPECT_EQ(output[2], 2250); 
    EXPECT_EQ(output[3], 4000);
    EXPECT_EQ(output[4], 5000);
}

TEST_F(SoundProcessorTest, ConfigParserValid)
{
    std::string config = R"(
        # Тестовая конфигурация
        mute 0 1.5
        mix $2 2.0
        echo 0.3 0.6
    )";

    auto config_path = test_dir / "config.txt";
    std::ofstream(config_path) << config;

    ConfigParser parser(3); 
    auto configs = parser.parse(config_path.string());

    EXPECT_EQ(configs.size(), 3);
    EXPECT_EQ(configs[0].name, "mute");
    EXPECT_EQ(configs[1].name, "mix");
    EXPECT_EQ(configs[2].name, "echo");
    EXPECT_EQ(configs[1].secondary_stream_index, 1); 
}

TEST_F(SoundProcessorTest, ConfigParserInvalid)
{
    std::string config = "unknown_command 1 2 3";
    auto config_path = test_dir / "bad_config.txt";
    std::ofstream(config_path) << config;

    ConfigParser parser(2);
    EXPECT_THROW({ parser.parse(config_path.string()); }, std::runtime_error);
}

TEST_F(SoundProcessorTest, ConfigParserStreamReference)
{
    std::string config = "mix $3 1.0";
    auto config_path = test_dir / "stream_ref.txt";
    std::ofstream(config_path) << config;

    ConfigParser parser(3); 
    EXPECT_NO_THROW({
        auto configs = parser.parse(config_path.string());
        EXPECT_EQ(configs[0].secondary_stream_index, 2);
    });

    ConfigParser parser2(2); 
    EXPECT_THROW({ parser2.parse(config_path.string()); }, std::runtime_error);
}


TEST_F(SoundProcessorTest, IntegrationTest)
{

    auto main_samples = GenerateTestSamples(44100);
    auto secondary_samples = std::vector<int16_t>(22050, 2000); 

    auto main_path = test_dir / "main.wav";
    auto secondary_path = test_dir / "secondary.wav";
    auto output_path = test_dir / "output.wav";
    auto config_path = test_dir / "config.txt";

    CreateTestWavFile(main_path, main_samples);
    CreateTestWavFile(secondary_path, secondary_samples);


    std::string config = R"(
        mute 0 0.1
        mix $2 0.2

    )";
    std::ofstream(config_path) << config;


    EXPECT_TRUE(fs::exists(main_path));
    EXPECT_TRUE(fs::exists(secondary_path));
    EXPECT_TRUE(fs::exists(config_path));


    ConfigParser parser(2);
    auto configs = parser.parse(config_path.string());
    EXPECT_EQ(configs.size(), 2);

    std::vector<std::vector<int16_t>> streams = { main_samples,
                                                  secondary_samples };
    auto converters = parser.CreateConverters(configs, streams);
    EXPECT_EQ(converters.size(), 2);
}


TEST_F(SoundProcessorTest, EdgeCases)
{
 
    auto empty_path = test_dir / "empty.wav";
    std::ofstream(empty_path).close();

    WavFile wav;
    EXPECT_FALSE(wav.OpenForRead(empty_path.string()));




    std::vector<int16_t> secondary = { 1000 };
    MixConverter mix(0, 100.0); 
    mix.Prepare(44100);
    mix.SetSecondaryStream(&secondary);

    std::vector<int16_t> main_input = { 500 };
    std::vector<int16_t> mix_result(1);

    mix.Process(main_input.data(), mix_result.data(), 1);
    EXPECT_EQ(mix_result[0], 500); 
}


TEST_F(SoundProcessorTest, ErrorHandling)
{

    ConfigParser parser(1);
    EXPECT_THROW({ parser.parse("nonexistent.txt"); }, std::runtime_error);

    // Некорректные числа в конфигурации
    std::string bad_config = "mute not_a_number 2.5";
    auto bad_path = test_dir / "bad.txt";
    std::ofstream(bad_path) << bad_config;

    EXPECT_THROW({ parser.parse(bad_path.string()); }, std::runtime_error);


    std::string bad_echo = "echo -1 2.0"; 
    auto echo_path = test_dir / "echo_bad.txt";
    std::ofstream(echo_path) << bad_echo;

    EXPECT_THROW({ parser.parse(echo_path.string()); }, std::runtime_error);
}

int main(int argc, char** argv)
{
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}