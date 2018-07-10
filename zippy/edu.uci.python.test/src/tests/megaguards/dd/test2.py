a = [[1, 2, 3], [1, 2, 3], [1, 2, 3]]
def t():
    for i in range(len(a)):
        a[i][i] = a[i][i]*2
t()
print(a)
