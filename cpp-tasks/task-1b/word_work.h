#pragma once
#include <algorithm>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <map>
#include <sstream>
#include <string>
#include <vector>

using namespace std;

map<string, int> CountWordFromFile(ifstream& input);

void WriteCSV(ofstream& output,
              vector<pair<string, int>> sorted_word_count,
              int total_words);
