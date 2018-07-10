a = [[1, 2, 3], [1, 2, 3], [1, 2, 3]]
b = [4, 5, 6]
c = [[1, 2, 3], [1, 2, 3], [1, 2, 3]]
def callme():
  for i in range(len(a)):
    for j in range(a[i][0]-1, a[i][2]):
        c[i][j] = a[i][j] * 2

callme()
print(c)
