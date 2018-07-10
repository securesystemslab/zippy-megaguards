size = 8
a = [2 * n for n in range(size * size)]
Sums = [-i for i in range(size)]
def t():
    for k in range(3):
        Sum = 0
        for i in range(size):
            for j in range(size):
                tmp = a[i * size + j]
                Sum += tmp

        for i in range(size):
            Sums[i] += Sum - a[i]

t()
print(Sums)
