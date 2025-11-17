#include "bit_array.h"
#include "gtest/gtest.h"
#include <stdexcept>


using namespace bit_array;

TEST(BitArrayTest, ConstructorsAndDestructor)
{

    BitArray ba1;
    EXPECT_TRUE(ba1.empty());
    EXPECT_EQ(ba1.size(), 0);
    EXPECT_EQ(ba1.count(), 0);


    BitArray ba2(10);
    EXPECT_FALSE(ba2.empty());
    EXPECT_EQ(ba2.size(), 10);
    EXPECT_EQ(ba2.count(), 0);

    BitArray ba3(8, 0b10101010);
    EXPECT_EQ(ba3.size(), 8);
    EXPECT_EQ(ba3.count(), 4);
    EXPECT_FALSE(ba3[0]);
    EXPECT_TRUE(ba3[1]);
    EXPECT_FALSE(ba3[2]);
    EXPECT_TRUE(ba3[3]);

    BitArray ba4(ba3);
    EXPECT_EQ(ba4.size(), 8);
    EXPECT_EQ(ba4.count(), 4);
    EXPECT_FALSE(ba4[0]);
    EXPECT_TRUE(ba4[1]);


    ba4.reset();
    EXPECT_FALSE(ba4[7]);
    EXPECT_TRUE(ba3[7]); 
}


TEST(BitArrayTest, AssignmentAndSwap)
{
    BitArray ba1(8, 0b11001100);
    BitArray ba2(4, 0b1010);


    ba2 = ba1;
    EXPECT_EQ(ba2.size(), 8);
    EXPECT_EQ(ba2.count(), 4);
    EXPECT_TRUE(ba2[2]);
    EXPECT_TRUE(ba2[3]);


    ba2 = ba2;
    EXPECT_EQ(ba2.size(), 8);
    EXPECT_EQ(ba2.count(), 4);

    BitArray ba3(6, 0b111000);
    BitArray ba4(4, 0b1010);

    size_t size3 = ba3.size();
    size_t size4 = ba4.size();
    int count3 = ba3.count();
    int count4 = ba4.count();

    ba3.swap(ba4);

    EXPECT_EQ(ba3.size(), size4);
    EXPECT_EQ(ba4.size(), size3);
    EXPECT_EQ(ba3.count(), count4);
    EXPECT_EQ(ba4.count(), count3);
}


TEST(BitArrayTest, Resize)
{
    BitArray ba(8, 0b11001100);

    ba.resize(4);
    EXPECT_EQ(ba.size(), 4);
    EXPECT_EQ(ba.count(), 2);
    EXPECT_FALSE(ba[0]); 
    EXPECT_FALSE(ba[1]);
    EXPECT_TRUE(ba[2]);
    EXPECT_TRUE(ba[3]);

    ba.resize(8, false);
    EXPECT_EQ(ba.size(), 8);
    EXPECT_EQ(ba.count(), 2);
    for (int i = 4; i < 8; ++i) {
        EXPECT_FALSE(ba[i]);
    }

    BitArray ba2(4, 0b1010);
    ba2.resize(8, true);
    EXPECT_EQ(ba2.size(), 8);
    EXPECT_EQ(ba2.count(), 6);
    for (int i = 4; i < 8; ++i) {
        EXPECT_TRUE(ba2[i]);
    }


    ba2.resize(0);
    EXPECT_TRUE(ba2.empty());
}


TEST(BitArrayTest, ClearEmptySize)
{
    BitArray ba(10, 0b1111111111);

    EXPECT_FALSE(ba.empty());
    EXPECT_EQ(ba.size(), 10);
    EXPECT_EQ(ba.count(), 10);

    ba.clear();
    EXPECT_TRUE(ba.empty());
    EXPECT_EQ(ba.size(), 0);
    EXPECT_EQ(ba.count(), 0);


    BitArray ba2;
    ba2.clear();
    EXPECT_TRUE(ba2.empty());
}


TEST(BitArrayTest, SetReset)
{
    BitArray ba(8);

    ba.set(0);
    ba.set(3);
    ba.set(7);
    EXPECT_TRUE(ba[0]);
    EXPECT_TRUE(ba[3]);
    EXPECT_TRUE(ba[7]);
    EXPECT_EQ(ba.count(), 3);


    ba.reset(3);
    EXPECT_FALSE(ba[3]);
    EXPECT_EQ(ba.count(), 2);


    ba.set(0, false);
    EXPECT_FALSE(ba[0]);
    EXPECT_EQ(ba.count(), 1);


    ba.set();
    for (int i = 0; i < 8; ++i) {
        EXPECT_TRUE(ba[i]);
    }
    EXPECT_EQ(ba.count(), 8);


    ba.reset();
    for (int i = 0; i < 8; ++i) {
        EXPECT_FALSE(ba[i]);
    }
    EXPECT_EQ(ba.count(), 0);
}


TEST(BitArrayTest, AccessorsAndOperators)
{
    BitArray ba(8, 0b10101010);

    EXPECT_FALSE(ba[0]); // LSB
    EXPECT_TRUE(ba[1]);
    EXPECT_FALSE(ba[2]);
    EXPECT_TRUE(ba[3]);
    EXPECT_FALSE(ba[4]);
    EXPECT_TRUE(ba[5]);
    EXPECT_FALSE(ba[6]);
    EXPECT_TRUE(ba[7]); // MSB

    EXPECT_TRUE(ba.any());
    EXPECT_FALSE(ba.none());

    BitArray empty_ba;
    EXPECT_FALSE(empty_ba.any());
    EXPECT_TRUE(empty_ba.none());

    BitArray zero_ba(8);
    EXPECT_FALSE(zero_ba.any());
    EXPECT_TRUE(zero_ba.none());


    EXPECT_EQ(ba.count(), 4);

    BitArray inverted = ~ba;
    EXPECT_EQ(inverted.size(), 8);
    EXPECT_EQ(inverted.count(), 4);
    EXPECT_TRUE(inverted[0]);
    EXPECT_FALSE(inverted[1]);
    EXPECT_TRUE(inverted[2]);
    EXPECT_FALSE(inverted[3]);

    BitArray double_inverted = ~~ba;
    for (int i = 0; i < 8; ++i) {
        EXPECT_EQ(double_inverted[i], ba[i]);
    }
}

TEST(BitArrayTest, Shifts)
{
    BitArray ba(8, 0b00001111); 


    BitArray shifted_left = ba << 2;
    EXPECT_EQ(shifted_left.size(), 8);
    EXPECT_TRUE(shifted_left[2]);
    EXPECT_TRUE(shifted_left[3]);
    EXPECT_TRUE(shifted_left[4]);
    EXPECT_TRUE(shifted_left[5]);
    EXPECT_FALSE(shifted_left[0]); 
    EXPECT_FALSE(shifted_left[1]);


    BitArray shifted_right = ba >> 2;
    EXPECT_EQ(shifted_right.size(), 8);
    EXPECT_TRUE(shifted_right[0]); 
    EXPECT_TRUE(shifted_right[1]);
    EXPECT_FALSE(shifted_right[6]);
    EXPECT_FALSE(shifted_right[7]);


    BitArray ba2 = ba;
    ba2 <<= 1;
    EXPECT_TRUE(ba2[1]);
    EXPECT_TRUE(ba2[2]);
    EXPECT_TRUE(ba2[3]);
    EXPECT_TRUE(ba2[4]);
    EXPECT_FALSE(ba2[0]);

    BitArray ba3 = ba;
    ba3 >>= 1;
    EXPECT_TRUE(ba3[0]);
    EXPECT_TRUE(ba3[1]);
    EXPECT_TRUE(ba3[2]);
    EXPECT_FALSE(ba3[7]);


    BitArray ba4(4, 0b1111);
    ba4 <<= 10; 
    EXPECT_EQ(ba4.count(), 0);

    ba4.set();
    ba4 >>= 10; 
    EXPECT_EQ(ba4.count(), 0);

    BitArray ba5 = ba;
    ba5 <<= 0;
    for (int i = 0; i < 8; ++i) {
        EXPECT_EQ(ba5[i], ba[i]);
    }
}


TEST(BitArrayTest, BitwiseOperationsSameSize)
{
    BitArray ba1(8, 0b11001100);
    BitArray ba2(8, 0b10101010);

    // AND
    BitArray and_result = ba1 & ba2;
    EXPECT_EQ(and_result.size(), 8);
    EXPECT_EQ(and_result.count(), 2);
    EXPECT_FALSE(and_result[0]);
    EXPECT_FALSE(and_result[1]);
    EXPECT_FALSE(and_result[2]);
    EXPECT_TRUE(and_result[3]);


    // OR
    BitArray or_result = ba1 | ba2;
    EXPECT_EQ(or_result.size(), 8);
    EXPECT_EQ(or_result.count(), 6);
    EXPECT_FALSE(or_result[0]);
    EXPECT_TRUE(or_result[1]);
    EXPECT_TRUE(or_result[2]);
    EXPECT_TRUE(or_result[3]);


    // XOR
    BitArray xor_result = ba1 ^ ba2;
    EXPECT_EQ(xor_result.size(), 8);
    EXPECT_EQ(xor_result.count(), 4);
    EXPECT_FALSE(xor_result[0]);
    EXPECT_TRUE(xor_result[1]);
    EXPECT_TRUE(xor_result[2]);
    EXPECT_FALSE(xor_result[3]);



    BitArray ba3 = ba1;
    ba3 &= ba2;
    for (int i = 0; i < 8; ++i) {
        EXPECT_EQ(ba3[i], and_result[i]);
    }

    BitArray ba4 = ba1;
    ba4 |= ba2;
    for (int i = 0; i < 8; ++i) {
        EXPECT_EQ(ba4[i], or_result[i]);
    }

    BitArray ba5 = ba1;
    ba5 ^= ba2;
    for (int i = 0; i < 8; ++i) {
        EXPECT_EQ(ba5[i], xor_result[i]);
    }
}


TEST(BitArrayTest, BitwiseOperationsDifferentSizes)
{
    BitArray ba1(8);
    BitArray ba2(4);

    // AND
    EXPECT_THROW(ba1 & ba2, std::invalid_argument);
    EXPECT_THROW(ba1 &= ba2, std::invalid_argument);

    // OR
    EXPECT_THROW(ba1 | ba2, std::invalid_argument);
    EXPECT_THROW(ba1 |= ba2, std::invalid_argument);

    // XOR
    EXPECT_THROW(ba1 ^ ba2, std::invalid_argument);
    EXPECT_THROW(ba1 ^= ba2, std::invalid_argument);
}


TEST(BitArrayTest, ComparisonOperators)
{
    BitArray ba1(8, 0b11001100);
    BitArray ba2(8, 0b11001100);
    BitArray ba3(8, 0b10101010);
    BitArray ba4(4, 0b1100);


    EXPECT_TRUE(ba1 == ba2);
    EXPECT_FALSE(ba1 != ba2);


    EXPECT_FALSE(ba1 == ba3);
    EXPECT_TRUE(ba1 != ba3);


    EXPECT_FALSE(ba1 == ba4);
    EXPECT_TRUE(ba1 != ba4);


    BitArray empty1, empty2;
    EXPECT_TRUE(empty1 == empty2);
    EXPECT_FALSE(empty1 != empty2);
}


TEST(BitArrayTest, EdgeCasesAndExceptions)
{
    BitArray ba(4);


    EXPECT_THROW(ba.set(10), std::out_of_range);
    EXPECT_THROW(ba.reset(10), std::out_of_range);
    EXPECT_THROW(ba[10], std::out_of_range);
    EXPECT_THROW(ba[-1], std::out_of_range);


    EXPECT_THROW(BitArray(-1), std::invalid_argument);
    EXPECT_THROW(ba.resize(-1), std::invalid_argument);


    BitArray empty;
    EXPECT_FALSE(empty.any());
    EXPECT_TRUE(empty.none());
    EXPECT_EQ(empty.count(), 0);
    EXPECT_EQ(empty.size(), 0);
    EXPECT_TRUE(empty.empty());


    EXPECT_EQ(empty.to_string(), "");
}

TEST(BitArrayTest, PushBack)
{
    BitArray ba;

    ba.push_back(true);
    EXPECT_EQ(ba.size(), 1);
    EXPECT_TRUE(ba[0]);
    EXPECT_EQ(ba.count(), 1);

    ba.push_back(false);
    EXPECT_EQ(ba.size(), 2);
    EXPECT_FALSE(ba[1]);
    EXPECT_EQ(ba.count(), 1);

    ba.push_back(true);
    EXPECT_EQ(ba.size(), 3);
    EXPECT_TRUE(ba[2]);
    EXPECT_EQ(ba.count(), 2);

    EXPECT_EQ(ba.to_string(), "101");
}


TEST(BitArrayTest, MoreWords)
{
    BitArray ba(65,0b1111);
    ba <<= 33 ;
    EXPECT_EQ(ba.count(), 4);
    EXPECT_EQ(ba[33], true);
}

int main(int argc, char** argv)
{
    ::testing::InitGoogleTest(&argc, argv);
    RUN_ALL_TESTS();
    return 1;
}