size = 8
a = [2 * n for n in range(size)]
Sum = [0]
def t():
    for i in range(size):
        tmp = a[i]
        Sum[0] += tmp
t()
print(Sum[0])
