import math
Nparticles=100

rand_c = [2**33, 16346, 18299, 26845, 3318, 11618, 7451, 23885, 8106, 5612,
-25751, -6603, 16672, 9990, 995, 22866, 10193, 7526, 10168, 11896, 25713,
5155, 12320, 13293, 15219, 29599, 5390, 3028, 366, 21923, 5613, 14867, 21406,
19266, 3220, 30169, 27043, 11558, 18747, 13467, 18691, 5918, 1601, 12722, 5706,
11882, 3981, 30949, 25200, 14199, 19720, 29994, 4180, 14997, 27215, 19117, 2185,
1375, 14566, 9724, 2522, 3018, 4821, 15780, 3884, 768, 24396, 1290, 29407, 12240,
29350, 10227, 23336, 8608, 10538, 15528, 8221, 20873, 20647, 22108, 21197, 32664,
11720, 1286, 31999, 10873, 23524, 11580, 31948, 24130, 470, 24387, 27500, 28827,
1403, 21907, 16863, 23527, 27110, 22557]
rand_c[0] = 30020;

seed=[rand_c[i] for i in range(Nparticles)]

arrayX=[64.0 for i in range(Nparticles)]
arrayY=[64.0 for i in range(Nparticles)]

PI = 3.1415926535897932

M = 32767 # value for Linear Congruential Generator (LCG)
A = 1103515245 # value for LCG
C = 12345 # value for LCG

# emulate cpp int overflow
max_int32 = 2**16
sub_max_int32 = 2**15
def cppNum(num):
    return (num + sub_max_int32) % max_int32 - sub_max_int32

def cppMod(num, x):
    return num % x if num > 0 else -1 * ((-1 * num) % x)

# Generates a uniformly distributed random number using the provided seed and GCC's settings for the Linear Congruential Generator (LCG)
def randu(seed, index):
    num = cppNum(A * seed[index] + C)
    seed[index] = cppMod(num, M)
    # seed[index] = num % M;
    return math.fabs(seed[index] / (float(M)));


# Generates a normally distributed random number using the Box-Muller transformation
def randn(seed, index):
    u = randu(seed, index);
    v = randu(seed, index);
    cosine = math.cos(2 * PI * v);
    rt = -2 * math.log(u);
    return math.sqrt(rt) * cosine;

def t():
    for x in range(Nparticles):
        arrayX[x] += 1 + 5 * randn(seed, x);
        arrayY[x] += -2 + 2 * randn(seed, x);

t()
t()

def print1D(x,c):
    limit = 128
    s = "["
    for i in range(c):
        if i % limit == limit-1:
            s += "\n"
        s += "%.8f, " % x[i]

    print(s+ "]")

print1D(arrayX, Nparticles)
print1D(arrayY, Nparticles)
