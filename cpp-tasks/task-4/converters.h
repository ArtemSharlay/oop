#ifndef CONVERTERS_H
#define CONVERTERS_H

#include <cstdint>
#include <functional>
#include <memory>
#include <string>
#include <unordered_map>
#include <vector>

class Converter
{
public:
    virtual ~Converter() = default;
    virtual void
    Process(const int16_t* input, int16_t* output, size_t num_samples) = 0;
    virtual void Prepare(size_t sample_rate) = 0;
    virtual bool RequiresSecondaryStream() const
    {
        return false;
    }
    virtual void SetSecondaryStream(const std::vector<int16_t>* stream)
    { }
    virtual void Reset()
    { }
    virtual std::string GetName() const = 0;
    virtual std::string GetDescription() const = 0;
    virtual std::string GetSyntax() const = 0;
};

class MuteConverter : public Converter
{
public:
    MuteConverter(double start_time, double end_time);
    void
    Process(const int16_t* input, int16_t* output, size_t num_samples) override;
    void Prepare(size_t sample_rate) override;
    std::string GetName() const override
    {
        return "mute";
    }
    std::string GetDescription() const override
    {
        return "mute [start, end) sec";
    }
    std::string GetSyntax() const override
    {
        return "mute <start> <end>";
    }

private:
    double start_time;
    double end_time;
    size_t start_sample;
    size_t end_sample;
    size_t current_sample;
};

class MixConverter : public Converter
{
public:
    MixConverter(int stream_index, double offset);
    void
    Process(const int16_t* input, int16_t* output, size_t num_samples) override;
    void Prepare(size_t sample_rate) override;
    bool RequiresSecondaryStream() const override
    {
        return true;
    }
    void SetSecondaryStream(const std::vector<int16_t>* stream) override;
    std::string GetName() const override
    {
        return "mix";
    }
    std::string GetDescription() const override
    {
        return "mix additional stream";
    }
    std::string GetSyntax() const override
    {
        return "mix $<n> [offset]";
    }

private:
    int stream_index;
    double offset;
    size_t offset_samples;
    const std::vector<int16_t>* secondary_stream;
    size_t current_sample;
};

class EchoConverter : public Converter
{
public:
    EchoConverter(double delay, double decay);
    void
    Process(const int16_t* input, int16_t* output, size_t num_samples) override;
    void Prepare(size_t sample_rate) override;
    void Reset() override;
    std::string GetName() const override
    {
        return "echo";
    }
    std::string GetDescription() const override
    {
        return "echo effect";
    }
    std::string GetSyntax() const override
    {
        return "echo <delay> <decay>";
    }

private:
    double delay;
    double decay;
    size_t delay_samples;
    std::vector<int16_t> buffer;
    size_t buffer_pos;
};

class ConverterFactory
{
public:
    using Creator = std::function<std::unique_ptr<Converter>(
        const std::vector<std::string>& args)>;

    static ConverterFactory& GetInstance();

    void RegisterConverter(const std::string& name,
                           Creator creator,
                           const std::string& description,
                           const std::string& syntax);

    std::unique_ptr<Converter>
    CreateConverter(const std::string& name,
                    const std::vector<std::string>& args);

    std::vector<std::string> GetAvailableConverters() const;
    std::string GetConverterInfo(const std::string& name) const;
    std::string GetHelpText() const;

private:
    ConverterFactory();
    void RegisterDefaults();

    struct ConverterInfo
    {
        Creator creator;
        std::string description;
        std::string syntax;
    };

    std::unordered_map<std::string, ConverterInfo> converters;
};

int16_t Clamp16(int32_t value);

#endif