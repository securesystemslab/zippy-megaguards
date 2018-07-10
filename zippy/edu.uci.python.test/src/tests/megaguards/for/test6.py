a = [[1, 2, 3], [1, 2, 3], [1, 2, 3]]
b = [4, 5, 6]
c = [0, 0, 0]
def callme():
  for j in range(len(a)):
    for [A1, A2, A3] in a:
      for B in b:
        c[A1-1] = B + A1*2
        c[A2-1] = B + A2*2
        c[A3-1] = B + A3*2
for i in range(3):
  callme()
print(c)
