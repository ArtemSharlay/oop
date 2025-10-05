#include "word_work.h"

int main(int arg_count, char* arg_vec[])
{
    string input_file_name = arg_vec[1];
    string output_file_name = arg_vec[2];

    ifstream input(input_file_name);
    if (!input.is_open()) {
        cerr << "open error" << endl;
        return 1;
    }

    map<string, int> word_count;
    word_count = CountWordFromFile(input);
    input.close();

    vector<pair<string, int>> sorted_word_count(word_count.begin(),
                                                word_count.end());
    sort(sorted_word_count.begin(),
         sorted_word_count.end(),
         [](const auto& a, const auto& b) { return a.second > b.second; });

    int total_words = 0;
    for (const auto& pair : word_count) {
        total_words += pair.second;
    }

    ofstream output(output_file_name);
    if (!output.is_open()) {
        cerr << "error" << endl;
        return 1;
    }



    WriteCSV(output, sorted_word_count, total_words);

    output.close();
    cout << "done " << output_file_name << endl;

    return 0;
}
