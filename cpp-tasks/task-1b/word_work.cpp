#include "word_work.h"
using namespace std;

map<string, int> CountWordFromFile(ifstream& input)
{
    map<string, int> word_count;
    string cleaned, line;
    while (getline(input, line)) {
        for (char c : line) {
            if (isalnum(c)) {
                cleaned += tolower(c);
            }
            else {
                if (!cleaned.empty()) {
                    word_count[cleaned]++;
                }
                cleaned.clear();
            }
        }
        if (!cleaned.empty()) {
            word_count[cleaned]++;
        }
        cleaned.clear();
    }
    return word_count;
}

void WriteCSV(ofstream& output,
              vector<pair<string, int>> sorted_word_count,
              int total_words)
{
    output << "word,count,percent\n";
    for (const auto& pair : sorted_word_count) {
        double percentage =
            static_cast<double>(pair.second) / total_words * 100;
        output << pair.first << "," << pair.second << "," << fixed
               << setprecision(2) << percentage << "\n";
    }
}