size = 8
a = [2 * n for n in range(size * size)]
Sums = [0 for i in range(6)]
def t():
    for k in range(3):
        Sum = 2
        Sum2 = 1
        for i in range(size):
            for j in range(size):
                tmp = a[i * size + j]
                Sum += tmp
                Sum2 += tmp*tmp

        Sums[k] = Sum
        Sums[k + 3] = Sum
t()
print(Sums)
