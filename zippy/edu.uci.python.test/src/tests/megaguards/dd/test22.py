size = 8
a = [2 * n for n in range(size)]
Sums = [0]
def t():
    for k in range(3):
        Sum = 0
        x = 0
        for i in range(size):
            x = i
            Sum += a[i]

        Sums[0] += Sum + x

t()
print(Sums)
