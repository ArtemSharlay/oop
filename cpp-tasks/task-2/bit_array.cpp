#include "bit_array.h"
#include <algorithm>
#include <cstring>
#include <iostream>
#include <stdexcept>

// ¬ этой задаче дл€ простоты не требуетс€ делать контейнер шаблонным.
// ѕо желанию можно выбрать базовый тип хранени€: 8/16/32/64-битные беззнаковые.
namespace bit_array
{
namespace
{
    const char kLogBitsPerWord = 5;
    const char kWordBits = 32;
}

size_t BitArray::word_index(size_t bit_index) const
{
    return bit_index >> kLogBitsPerWord;
}

uint32_t BitArray::bit_mask(size_t bit_index) const
{
    return (uint32_t(1) << (kWordBits - 1)) >> (bit_index & (kWordBits - 1));
}

BitArray::BitArray()
    : data(nullptr)
    , array_size(0)
    , capacity(0)
{ }

BitArray::~BitArray()
{
    delete[] data;
    data = nullptr;
    array_size = 0;
    capacity = 0;
}

// Construct an array of num_bits bits.
// The lowest sizeof(unsigned long)*8 bits can be initialized from 'value'.
BitArray::BitArray(int num_bits, unsigned long value)
    : data(nullptr)
    , array_size(0)
    , capacity(0)
{
    if (num_bits < 0) {
        throw std::invalid_argument("Number of bits cannot be negative");
    }

    array_size = num_bits;

    if (array_size > 0) {
        capacity = (array_size + kWordBits - 1) / kWordBits;
        data = new uint32_t[capacity]();
        if (value != 0) {
            for (int i = 0; i < array_size && i < static_cast<int>(kWordBits);
                 ++i)
            {
                if ((value & (1 << i))) {
                    data[0] |= 1 << ((kWordBits - 1) - i);
                }
            }
        }
    }
}

BitArray::BitArray(const BitArray& b)
    : array_size(b.array_size)
    , capacity(b.capacity)
{
    if (capacity > 0) {
        data = new uint32_t[capacity];
        std::copy(
            b.data, b.data + (array_size + kWordBits - 1) / kWordBits, data);
    }
    else {
        data = nullptr;
    }
}

// Swap contents with another BitArray. No-throw guarantee.
void BitArray::swap(BitArray& b)
{
    std::swap(data, b.data);
    std::swap(array_size, b.array_size);
    std::swap(capacity, b.capacity);
}

BitArray& BitArray::operator=(const BitArray& b)
{
    if (this != &b) {
        delete[] data;

        array_size = b.array_size;
        capacity = b.capacity;

        if (capacity > 0) {
            data = new uint32_t[capacity];
            std::copy(b.data,
                      b.data + (array_size + kWordBits - 1) / kWordBits,
                      data);
        }
        else {
            data = nullptr;
        }
    }
    return *this;
}

// Change the size to num_bits.
// On growth, new bits are initialized with 'value'.
void BitArray::resize(int num_bits, bool value)
{
    if (num_bits < 0) {
        throw std::invalid_argument("Number of bits cannot be negative");
    }

    size_t new_size = num_bits;
    if (new_size == array_size) {
        return;
    }

    if (new_size > array_size) {
        size_t new_capacity = (new_size + kWordBits - 1) / kWordBits;

        ensure_capacity(new_capacity);
    }

    size_t old_size = array_size;
    
    if (old_size > new_size) {
        for (size_t i = new_size; i < old_size; ++i) {
            reset(i);
        }
        array_size = new_size;
        return;
    }

    array_size = new_size;
    if (value) {
        for (size_t i = old_size; i < array_size; ++i) {
            set(i);
        }
    }
}

// Remove all bits; size becomes 0. Capacity may be released.
void BitArray::clear()
{
    array_size = 0;
    delete[] data;
    data = nullptr;
    capacity = 0;
}

// Append one bit at the end; reallocate if needed.
void BitArray::push_back(bool bit)
{
    size_t new_size = array_size + 1;
    size_t words_needed = (new_size + kWordBits - 1) / kWordBits;
    ensure_capacity(words_needed);

    array_size = new_size;
    if (bit) {
        set(array_size - 1);
    }
    else {
        reset(array_size - 1);
    }
}

// Bitwise ops (sizes must match; otherwise throw std::invalid_argument).
BitArray& BitArray::operator&=(const BitArray& b)
{
    if (array_size != b.array_size) {
        throw std::invalid_argument("BitArrays must be of same size");
    }

    size_t words = capacity;
    for (size_t i = 0; i < words; ++i) {
        data[i] &= b.data[i];
    }
    return *this;
}
BitArray& BitArray::operator|=(const BitArray& b)
{
    if (array_size != b.array_size) {
        throw std::invalid_argument("BitArrays must be of same size");
    }

    size_t words = capacity;
    for (size_t i = 0; i < words; ++i) {
        data[i] |= b.data[i];
    }
    return *this;
}
BitArray& BitArray::operator^=(const BitArray& b)
{
    if (array_size != b.array_size) {
        throw std::invalid_argument("BitArrays must be of same size");
    }

    size_t words = capacity;
    for (size_t i = 0; i < words; ++i) {
        data[i] ^= b.data[i];
    }
    return *this;
}

// Logical shifts with zero fill.
// Shift left: towards higher indices. Shift right: towards lower indices.
BitArray& BitArray::operator<<=(int n) 
{
    if (n < 0)
        return *this >>= (-n);
    if (n == 0)
        return *this;
    if (n >= static_cast<int>(array_size)) {
        return reset();
    }

    for (int i = array_size - 1; i >= n; --i) {
        bool bit_val = (*this)[i - n];
        set(i, bit_val);
    }

    for (int i = 0; i < n; ++i) {
        reset(i);
    }

    return *this;
}

BitArray& BitArray::operator>>=(int n) 
{
    if (n < 0)
        return *this <<= (-n);
    if (n == 0)
        return *this;
    if (n >= static_cast<int>(array_size)) {
        return reset();
    }

    for (int i = n; i < static_cast<int>(array_size); ++i) {
        bool bit_val = (*this)[i];
        set(i - n, bit_val);
    }

    for (int i = array_size - n; i < array_size; ++i) { 
        reset(i);
    }

    return *this;
}
BitArray BitArray::operator<<(int n) const
{
    BitArray result(*this);
    result <<= n;
    return result;
}

BitArray BitArray::operator>>(int n) const
{
    BitArray result(*this);
    result >>= n;
    return result;
}

// Set bit at index n to 'val'. Throws std::out_of_range on bad index.
BitArray& BitArray::set(int n, bool val)
{

    if (n < 0 || n >= static_cast<int>(array_size)) {
        throw std::out_of_range("Bit index out of range");
    }

    size_t word = word_index(n);
    uint32_t mask = bit_mask(n);

    if (val) {
        data[word] |= mask;
    }
    else {
        data[word] &= ~mask;
    }
    return *this;
}

void BitArray::ensure_capacity(size_t new_capacity)
{
    if (new_capacity <= capacity)
        return;

    uint32_t* new_data = new uint32_t[new_capacity]();
    if (data && capacity > 0) {
        std::copy(data, data + capacity, new_data);
        delete[] data;
    }
    data = new_data;
    capacity = new_capacity;
}

// Set all bits to 1 (true).
BitArray& BitArray::set()
{
    if (array_size > 0) {
        size_t words = (array_size + kWordBits - 1) / kWordBits;

        for (size_t i = 0; i < words; ++i) {
            data[i] = ~uint32_t(0);
        }

        size_t extra_bits = array_size % kWordBits;
        if (extra_bits != 0) {
            uint32_t mask = ((1UL << extra_bits) - 1)
                            << (kWordBits - extra_bits);
            data[words - 1] &= mask;
        }
    }
    return *this;
}

// Reset bit at index n to 0 (false). Throws std::out_of_range on bad
// index.
BitArray& BitArray::reset(int n)
{
    return set(n, false);
}
// Set all bits to 0 (false).
BitArray& BitArray::reset()
{
    if (array_size > 0) {
        size_t words = (array_size + kWordBits - 1) / kWordBits;

        for (size_t i = 0; i < words; ++i) {
            data[i] = 0;
        }
    }
    return *this;
}

// Returns true if there is at least one bit set to 1.
bool BitArray::any() const
{
    size_t words = (array_size + kWordBits - 1) / kWordBits;
    for (int i = 0; i < words; i++) {
        if (data[i] != 0) {
            return true;
        }
    }
    return false;
}
// Returns true if all bits are 0 (or size()==0).
bool BitArray::none() const
{
    return !any();
}

// Bitwise NOT (inversion), size-preserving.
BitArray BitArray::operator~() const
{
    BitArray result(*this);
    if (array_size > 0) {
        size_t words = (array_size + kWordBits - 1) / kWordBits;
        for (size_t i = 0; i < words; ++i) {
            result.data[i] = ~data[i];
        }
        size_t extra_bits = array_size % kWordBits;
        if (extra_bits != 0) {
            uint32_t mask = ((1UL << extra_bits) - 1)
                            << (kWordBits - extra_bits);

            result.data[words - 1] &= mask;
        }
    }
    return result;
}

// Count number of 1-bits. Returns 0..size().
int BitArray::count() const
{
    int result = 0;
    size_t words = (array_size + kWordBits - 1) / kWordBits;
    for (size_t i = 0; i < words; ++i) {
        uint32_t word = data[i];
        word = word - ((word >> 1) & 0x55555555);
        word = (word & 0x33333333) + ((word >> 2) & 0x33333333);
        result += (((word + (word >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
    }
    return result;
}

// Read-only access to bit at index i. Throws std::out_of_range on bad
// index.
bool BitArray::operator[](int i) const
{
    if (i < 0 || i >= static_cast<int>(array_size)) {
        throw std::out_of_range("Bit index out of range");
    }

    size_t word = word_index(i);
    uint32_t mask = bit_mask(i);
    return (data[word] & mask) != 0;
}

// Number of bits currently stored.
int BitArray::size() const
{
    return static_cast<int>(array_size);
}

// True if size()==0.
bool BitArray::empty() const
{
    return array_size == 0;
}

// Return string representation: MSB (index size()-1) first, LSB (index 0)
// last.
std::string BitArray::to_string() const
{
    std::string result;
    result.reserve(array_size);
    for (int i = array_size - 1; i >= 0; --i) {
        result += ((*this)[i] ? '1' : '0');
    }
    return result;
}
}
// Equality: sizes must match and all bits equal.
bool operator==(const bit_array::BitArray& a, const bit_array::BitArray& b)
{
    if (a.size() != b.size()) {
        return false;
    }

    for (int i = 0; i < a.size(); i++) {
        if (a[i] != b[i]) {
            return false;
        }
    }
    return true;
}
bool operator!=(const bit_array::BitArray& a, const bit_array::BitArray& b)
{
    return !(a == b);
}

// Free bitwise operators. Throw std::invalid_argument on size mismatch.
bit_array::BitArray operator&(const bit_array::BitArray& b1,
                              const bit_array::BitArray& b2)
{
    if (b1.size() != b2.size()) {
        throw std::invalid_argument("size mismatch");
    }

    bit_array::BitArray result(b1);
    result &= b2;
    return result;
}
bit_array::BitArray operator|(const bit_array::BitArray& b1,
    const bit_array::BitArray& b2)
{
    if (b1.size() != b2.size()) {
        throw std::invalid_argument("size mismatch");
    }

    bit_array::BitArray result(b1);
    result |= b2;
    return result;
}
bit_array::BitArray operator^(const bit_array::BitArray& b1,
    const bit_array::BitArray& b2)
{
    if (b1.size() != b2.size()) {
        throw std::invalid_argument("size mismatch");
    }

    bit_array::BitArray result(b1);
    result ^= b2;
    return result;
}


