size = 8
a = [2 * n for n in range(size * size)]
Sums = [0 for i in range(3)]
def t():
    for k in range(3):
        Sum = 0
        for i in range(size):
            for j in range(size):
                tmp = a[i * size + j]
                Sum += tmp

        Sums[k] = Sum
t()
print(Sums)
