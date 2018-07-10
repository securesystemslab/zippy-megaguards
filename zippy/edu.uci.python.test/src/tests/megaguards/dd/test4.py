a = [[1, 2, 3], [1, 2, 3], [1, 2, 3]]
def t():
    for i in range(len(a)):
        a[0][i] = a[0][0]*2
t()
print(a[0])
