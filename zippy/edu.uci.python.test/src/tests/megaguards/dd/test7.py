a = [1.,2.,3.,4.]
def atomic():
    b = 0.
    for i in range(3):
        for j in range(4):
            x = a[j]
        b += a[i]
    return b

b = atomic()
print(b)
