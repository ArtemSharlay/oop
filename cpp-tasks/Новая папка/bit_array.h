#pragma once

#include <string>
#include <vector>
#include "bit_array.h"


namespace bit_array
{

class BitArray
{
private:
    uint32_t* data;
    size_t array_size;
    size_t capacity;

    size_t word_index(size_t bit_index) const;

    uint32_t bit_mask(size_t bit_index) const;

public:
    /**
     * Default constructor - creates empty bit array
     */
    BitArray();
    /**
     * Default Destructor - releases allocated memmory
     */
    ~BitArray();

    explicit BitArray(int num_bits, unsigned long value = 0);

    BitArray(const BitArray& b);

    void swap(BitArray& b);

    BitArray& operator=(const BitArray& b);

    void resize(int num_bits, bool value = false);

    void clear();

    void push_back(bool bit);

    BitArray& operator&=(const BitArray& b);
    BitArray& operator|=(const BitArray& b);
    BitArray& operator^=(const BitArray& b);

    BitArray& operator<<=(int n);
    BitArray& operator>>=(int n);
    BitArray operator<<(int n) const;
    BitArray operator>>(int n) const;

    BitArray& set(int n, bool val = true);

    void ensure_capacity(size_t new_capacity);

    BitArray& set();

    BitArray& reset(int n);
    // Set all bits to 0 (false).
    BitArray& reset();

    // Returns true if there is at least one bit set to 1.
    bool any() const;
    // Returns true if all bits are 0 (or size()==0).
    bool none() const;

    // Bitwise NOT (inversion), size-preserving.
    BitArray operator~() const;

    // Count number of 1-bits. Returns 0..size().
    int count() const;

    // Read-only access to bit at index i. Throws std::out_of_range on bad
    // index.
    bool operator[](int i) const;

    // Number of bits currently stored.
    int size() const;

    // True if size()==0.
    bool empty() const;

    // Return string representation: MSB (index size()-1) first, LSB (index 0)
    // last.
    std::string to_string() const;
};
}
// Equality: sizes must match and all bits equal.
bool operator==(const bit_array::BitArray & a, const bit_array::BitArray& b);
bool operator!=(const bit_array::BitArray& a, const bit_array::BitArray& b);

// Free bitwise operators. Throw std::invalid_argument on size mismatch.
bit_array::BitArray operator&(const bit_array::BitArray& b1, const bit_array::BitArray& b2);
bit_array::BitArray operator|(const bit_array::BitArray& b1,
                              const bit_array::BitArray& b2);
bit_array::BitArray operator^(const bit_array::BitArray& b1,
                              const bit_array::BitArray& b2);



