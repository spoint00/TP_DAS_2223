#include <stdio.h>
#include "square.h"

int main() {
    int number, result;

    printf("Enter a number: ");
    scanf("%d", &number);

    result = calculateSquare(number);

    printf("Square of %d is %d\n", number, result);

    return 0;
}
