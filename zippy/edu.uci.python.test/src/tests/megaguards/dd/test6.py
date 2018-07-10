a = [[1, 2, 3], [1, 2, 3], [1, 2, 3]]

def callme():
  for i in range(len(a)):
    """@MG:ddoff"""
    a[0][i] = a[0][0]*2
  return a[0]
b = callme()
# print(b)
