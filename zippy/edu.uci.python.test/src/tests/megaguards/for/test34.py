size = 8
a = [2 * n for n in range(size * size)]

def t():
  for i in range(size):
    for j in range(i, size):
      Sum = a[i * size + j]
      for k in range(i):
        Sum -= a[i * size + k] * a[k * size + j]
      a[i * size + j] = Sum

t()
print(a)
