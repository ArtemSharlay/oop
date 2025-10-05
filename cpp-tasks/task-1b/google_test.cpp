#include "gtest/gtest.h"
#include "word_work.h"

ifstream CreateStringStream(const string& content)
{
    stringstream ss(content);
    string temp_filename = "test_input.txt";
    ofstream temp_file(temp_filename);
    temp_file << content;
    temp_file.close();
    return ifstream(temp_filename);
}

TEST(WordCounterTest, EmptyFile)
{
    string content = "";
    ifstream input = CreateStringStream(content);

    map<string, int> result = CountWordFromFile(input);

    EXPECT_EQ(result.size(), 0);
}

TEST(WordCounterTest, SingleWord)
{
    string content = "hello";
    ifstream input = CreateStringStream(content);

    map<string, int> result = CountWordFromFile(input);

    EXPECT_EQ(result.size(), 1);
    EXPECT_EQ(result["hello"], 1);
}

