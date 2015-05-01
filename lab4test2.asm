.data
value1:	.word	123
value2:	.word 	0x020

value3:	.word	40




.text
main: addi	$t1, $t1, 30
loop1: add		$t2, $t1, $t1
sub		$t3, $t2, $t1
addiu	$t4, $t4, 100
slt	$t5, $t3, $t2
subagain: addi	$10, $10, -5
beq $10, $11, skip
j	subagain
skip:	add $t9, $t3, $t2
addi	$a0, $a0, 0x020
lw	$a1, 0($a0)
sll		$10, $10, 28
sra		$10, $10, 8
addi	$10, $0, 1
sll		$10, $10, 31
srl		$10, $10, 10
lw $a1, 8($a0)
sw	$a1, 16($a0)
lw	$a3, value1
syscall