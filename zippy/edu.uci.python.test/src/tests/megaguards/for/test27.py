a = [1, 2, 3, 1, 2, 3, 1, 2, 3]
c = [1, 2, 3, 1, 2, 3, 1, 2, 3]

def callme():
  for i in range(3):
    for j in range(3):
        c[i*3 + j] = a[i*3+j] * 2

callme()
print(c)
