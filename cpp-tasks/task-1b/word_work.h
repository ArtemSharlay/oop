#pragma once
#include <algorithm>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <map>
#include <sstream>
#include <string>
#include <vector>


std::map<std::string, int> CountWordFromFile(std::ifstream& input);

void WriteCSV(std::ofstream& output,
              std::vector<std::pair<std::string, int>> sorted_word_count,
              int total_words);
