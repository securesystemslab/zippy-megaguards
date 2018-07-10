import math
a = [30020, 16346, 18299, 26845, 3318, 11618, 7451, 23885, 8106, 5612, -25751, -6603, 16672, 9990, 995, 22866, 10193]
c = [ 0. for i in range(len(a))]

PI = 3.1415926535897932

M = 32767 # value for Linear Congruential Generator (LCG)
A = 1103515245 # value for LCG
C = 12345 # value for LCG

def f(num):
    return (num + 2**31) % 2**32 - 2**31

def p(num, x):
    return num % x if num > 0 else -1 * ((-1 * num) % x)

def n(y, j):
    num = f(A * y[j] + C)
    y[j] = p(num, M)
    # seed[index] = num % M;
    return math.fabs(y[j] / (float(M)));


def m(y, j):
    u = n(y, j);
    v = n(y, j);
    cosine = math.cos(2 * PI * v);
    rt = -2 * math.log(u);
    return math.sqrt(rt) * cosine;

def w(x):
    return m(a, x)

def t():
    for i in range(len(a)):
        c[i] += 1 + 5 * w(i)


t()
# a[0] = 5.
# t()
limit = 64
s = "["
for i in range(len(c)):
    if i % limit == limit-1:
        s += "\n"
    s += "%.5f, " % c[i]

print(s+ "]")
