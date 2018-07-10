size = 8
a = [2 * n for n in range(size * size)]
Sums = [0 for i in range(3)]
def t():
    for k in range(3):
        Sum = 0
        for i in range(size):
            for j in range(size):
                if i % 2 == 0 and j % 2 == 0:
                    tmp = a[i * size + j]
                    Sum += tmp

        Sums[k] = Sum
t()
print(Sums)
