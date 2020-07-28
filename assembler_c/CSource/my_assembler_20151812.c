/*
 * ȭ�ϸ� : my_assembler_00000000.c 
 * ��  �� : �� ���α׷��� SIC/XE �ӽ��� ���� ������ Assembler ���α׷��� ���η�ƾ����,
 * �Էµ� ������ �ڵ� ��, ��ɾ �ش��ϴ� OPCODE�� ã�� ����Ѵ�.
 * ���� ������ ���Ǵ� ���ڿ� "00000000"���� �ڽ��� �й��� �����Ѵ�.
 */

/*
 *
 * ���α׷��� ����� �����Ѵ�. 
 *
 */
#pragma warning (disable : 4996)
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

// ���ϸ��� "00000000"�� �ڽ��� �й����� ������ ��.
#include "my_assembler_20151812.h"

/* ----------------------------------------------------------------------------------
 * ���� : ����ڷ� ���� ����� ������ �޾Ƽ� ��ɾ��� OPCODE�� ã�� ����Ѵ�.
 * �Ű� : ���� ����, ����� ���� 
 * ��ȯ : ���� = 0, ���� = < 0 
 * ���� : ���� ����� ���α׷��� ����Ʈ ������ �����ϴ� ��ƾ�� ������ �ʾҴ�. 
 *		   ���� �߰������� �������� �ʴ´�. 
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[]) 
{
	if(init_my_assembler()< 0)
	{
		printf("init_my_assembler: ���α׷� �ʱ�ȭ�� ���� �߽��ϴ�.\n"); 
		return -1 ; 
	}

	if(assem_pass1() < 0 ){
		printf("assem_pass1: �н�1 �������� �����Ͽ����ϴ�.  \n") ; 
		return -1 ; 
	}
	//make_opcode_output("output_20151812.txt");

	
	//������Ʈ���� ���Ǵ� �κ�
	make_symtab_output("symtab_20151812.txt");
	if(assem_pass2() < 0 ){
		printf(" assem_pass2: �н�2 �������� �����Ͽ����ϴ�.  \n") ; 
		return -1 ; 
	}
	

	make_objectcode_output("output_20151812.txt") ; 
	
	return 0;
}

/* ----------------------------------------------------------------------------------
 * ���� : ���α׷� �ʱ�ȭ�� ���� �ڷᱸ�� ���� �� ������ �д� �Լ��̴�. 
 * �Ű� : ����
 * ��ȯ : �������� = 0 , ���� �߻� = -1
 * ���� : ������ ��ɾ� ���̺��� ���ο� �������� �ʰ� ������ �����ϰ� �ϱ� 
 *		   ���ؼ� ���� ������ �����Ͽ� ���α׷� �ʱ�ȭ�� ���� ������ �о� �� �� �ֵ���
 *		   �����Ͽ���. 
 * ----------------------------------------------------------------------------------
 */
int init_my_assembler(void)
{
	int result ; 

	if((result = init_inst_file("inst_20151812.txt")) < 0 )
		return -1 ;
	if((result = init_input_file("input_20151812.txt")) < 0 )
		return -1 ; 
	return result ; 
}

/* ----------------------------------------------------------------------------------
 * ���� : �ӽ��� ���� ��� �ڵ��� ������ �о� ���� ��� ���̺�(inst_table)�� 
 *        �����ϴ� �Լ��̴�. 
 * �Ű� : ���� ��� ����
 * ��ȯ : �������� = 0 , ���� < 0 
 * ���� : ���� ������� ������ �����Ӱ� �����Ѵ�. ���ô� ������ ����.
 *	
 *	===============================================================================
 *		   | �̸� | ���� | ���� �ڵ� | ���۷����� ���� | NULL|
 *	===============================================================================	   
 *		
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char *inst_file)
{
	FILE *file;
	int errno;
	char str_buffer[10];//�ӽ÷� ���ڸ� �����ؼ� �޾ƿ� �������
	int buffer_len = 0;//str_buffer�� �γؽ�
	int condition = 0;//inst����ü�� ��� ������ ������ �� �� ������ �Ǻ�
	inst_index = 0;
	char buffer;//���Ͽ��� ���ڴ����� �޾ƿ�

	if ((file = fopen(inst_file, "r")) == NULL)
	{
		printf("inst������ �дµ� �����Ͽ����ϴ�\n");
		errno = -1;
		return errno;
	}
	else
	{
		errno = 0;
	}

	buffer = fgetc(file);

	while (buffer != EOF)
	{
		if ((buffer != '\t') && (buffer != '\n'))//��Ű��  ����Ű�� �ƴϸ� ����ؼ� ���ڿ��� ������
		{
			if (buffer != '\n')
			{
				str_buffer[buffer_len++] = buffer;
			}
			buffer = fgetc(file);
		}
		else if ((buffer == '\t') && (buffer != '\n'))//��Ű�̰� ����Ű�� �ƴ� ���
		{
			str_buffer[buffer_len] = '\0';//������ ���� ����
			if (condition == 0)//��ɾ� �̸� ����
			{
				if (inst_index >= MAX_INST)//���̻� ���� ������ ������ ������ �ν��ϰ� errno�� ����
				{
					errno = -1;
					return errno;
				}
				inst_table[inst_index] = (inst *)malloc(sizeof(inst)); //�߰� ���̺� �����Ҵ��� ����
				strcpy(inst_table[inst_index]->str, str_buffer);//���縦 ����
				buffer_len = 0;//���� ����
				memset(str_buffer, 0, strlen(str_buffer));//�迭�ʱ�ȭ
			}
			else if (condition == 1)//��ɾ��� ���� ����
			{
				if (strlen(str_buffer) != 1)//3/4�����̹Ƿ� ���ǻ� 3�� �־���
				{
					inst_table[inst_index]->format = 3;
				}
				else
				{
					inst_table[inst_index]->format = atoi(str_buffer);//������ ��ȯ�� �Ͽ� �־���
				}
				buffer_len = 0;//���� ����
				memset(str_buffer, 0, strlen(str_buffer));//�迭�ʱ�ȭ
			}
			else if (condition == 2)//��ɾ��� OPCODE����
			{
				char temp1;
				char temp2;
				unsigned char temp= 0;
				temp1 = str_buffer[0];
				temp2 = str_buffer[1];
				/*�� ���ڸ� ��ȯ*/
				//temp1�� 16�ڸ���
				if (temp1 >= 65) //���ĺ��� ��� ,�̶� 16���� �ΰ͵� ���� ���
				{
					temp = (temp1 - 55) * 16 + temp;
				}
				else
				{
					temp = (temp1 - 48) * 16 + temp;
				}
				//temp2�� 1���ڸ���
				if (temp2 >= 65) //���ĺ��� ���
				{
					temp =temp + (temp2 - 55);
				}
				else
				{
					temp =temp + (temp2 - 48);
				}
				inst_table[inst_index]->op = temp;//��ȯ�� ���ڸ� ����ü �ȿ��ٰ� �־���
				buffer_len = 0;//���� ����
				memset(str_buffer, 0, strlen(str_buffer));//�迭�ʱ�ȭ
			}
			/*���� ���ڰ� ���������� �޾ƿ�*/
			buffer = fgetc(file);
			while (buffer == '\t')
			{
				buffer = fgetc(file);
			}
			condition++;
		}
		else if (buffer == '\n')//����Ű�� ������ ������ ��
		{
			//�������� �־��ֱ�
			str_buffer[buffer_len] = '\0';//������ ���� ����
			if (condition == 3)
			{
				inst_table[inst_index++]->ops = atoi(str_buffer);//���ۿ��� �־��ְ� ���̺� ũ�� ����
				buffer_len = 0;//���� ����
				memset(str_buffer, 0, strlen(str_buffer));//�迭�ʱ�ȭ
			}
			condition = 0;
			buffer = fgetc(file);
		}
	}
	fclose(file);
	return errno;
}

/* ----------------------------------------------------------------------------------
 * ���� : ����� �� �ҽ��ڵ带 �о� �ҽ��ڵ� ���̺�(input_data)�� �����ϴ� �Լ��̴�. 
 * �Ű� : ������� �ҽ����ϸ�
 * ��ȯ : �������� = 0 , ���� < 0  
 * ���� : ���δ����� �����Ѵ�.
 *		
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char *input_file)
{
	FILE * file;
	int errno;
	char check[2000];
	line_num = 0;

	if ((file = fopen(input_file, "r")) == NULL)
	{
		printf("input������ �дµ� �����Ͽ����ϴ�\n");
		errno = -1;
	}
	else
	{
		errno = 0;
	}

	fgets(check, 2000, file);//���κ��� ���ڿ��� �о����

	while (strcmp(check,"") != 0)//���Ͽ��� ���̻� �޾ƿð� ���� ������ �޾ƿ�
	{
		if (line_num >= MAX_LINES)
		{
			errno = -1;
			return errno;
		}
		input_data[line_num] = (char*)malloc(2001 * sizeof(char));//���� ������ ������ �����ϴٸ� �����Ҵ�
		strcpy(input_data[line_num], check);//���ڿ��� �־���
		input_data[line_num][strlen(check)-1] = '\0';//���ڿ��� ������
		memset(check, 0, strlen(check));//�ٽ� �޾ƿ� ���ڿ��� ���� �ӽ� ���� ���� ����
		line_num++;//input_data ������ �÷���
		fgets(check, 2000, file);//���κ��� ���ڿ��� �о����
	}
	fclose(file);
	
	return errno;
}

/* ----------------------------------------------------------------------------------
 * ���� : �ҽ� �ڵ带 �о�� ��ū������ �м��ϰ� ��ū ���̺��� �ۼ��ϴ� �Լ��̴�. 
 *        �н� 1�� ���� ȣ��ȴ�. 
 * �Ű� : �Ľ��� ���ϴ� ���ڿ�  
 * ��ȯ : �������� = 0 , ���� < 0 
 * ���� : my_assembler ���α׷������� ���δ����� ��ū �� ������Ʈ ������ �ϰ� �ִ�. 
 * ----------------------------------------------------------------------------------
 */
int token_parsing(char *str) 
{
	char buffer[110]="";//�ӽ÷� ���ڸ� �����ؼ� �޾ƿ� �������
	int buffer_line=0;//str_buffer�� �ε��� ����
	char semi_buffer[30] = "";//���۷��带 ','�������� �ɰ�� ���� �����ϱ� ���� �޾ƿ� ���ڿ��� �ӽ� ����
	int semi_line = 0;//semi_buffer�� �ε���
	int condition = 0;//token����ü�� ��� ������ ������ �� �ٰ��� �����ϱ� ���� ����
	int i,j;
	int errno;
	errno = 0;
	if (token_line >= MAX_LINES)
	{
		errno = -1;
		return errno;
	}
	token_table[token_line] = (token*)malloc(sizeof(token));//��ū ���̺��� ���� �Ҵ����� �ϳ� �÷���
	/*�������� �ʱ�ȭ*/
	strcpy(token_table[token_line]->label, "");
	strcpy(token_table[token_line]->operator, "");
	for (i = 0; i < MAX_OPERAND; i++)
	{
		strcpy(token_table[token_line]->operand[i], "");
	}
	strcpy(token_table[token_line]->comment, "");
	token_table[token_line]->nixbpe = 1; //nixbpe�ʱ�ȭ
	for (i = 0; i <= (int)strlen(str); i++)//�� ������ ���̱��� ���
	{
		int mark_count = 0;
		if ((str[i] == '\t') || (str[i] == '\0') )//��Ű�̰ų� ������ ���϶�
		{
			if (condition == 0)//label�κп� ����
			{
				buffer[buffer_line] = '\0';//������ ���� ǥ�����ְ� �󺧺κκп� ����
				strcpy(token_table[token_line]->label, buffer); //�̰��� ����
				memset(buffer, 0, strlen(buffer));//�迭�ʱ�ȭ
				buffer_line = 0;
			}
			else if (condition == 1)//operator�κп� ����
			{
				buffer[buffer_line] = '\0';//������ ���� ǥ�����ְ� �󺧺κκп� ����
				strcpy(token_table[token_line]->operator, buffer); //�̰��� ���۷�����
				memset(buffer, 0, strlen(buffer));//�迭�ʱ�ȭ
				buffer_line = 0;
			}
			else if (condition == 2)//operand�κп� ����
			{
				buffer[buffer_line] = '\0';//������ ���� ǥ��
				int op_num = 0;
				for (j = 0; j < (int)strlen(buffer); j++)
				{
					if (buffer[j] == ',' || buffer[j] == '-')
					{
						semi_buffer[semi_line] = '\0';
						if (op_num >= MAX_OPERAND)//���� �� �ִ� ������ �Ѿ�� �����κ��̶� -1�� ����
						{
							errno = -1;
							return errno;
						}
						strcpy(token_table[token_line]->operand[op_num++], semi_buffer); //operand�κп� ����
						memset(semi_buffer, 0, strlen(semi_buffer));//�迭�ʱ�ȭ
						semi_line = 0;//���� �ʱ�ȭ
						operand_mark[mark_line][mark_count++] = buffer[j];
						continue;
					}
					else
					{
						semi_buffer[semi_line++] = buffer[j];//������������ ���� �ƴ϶�� ����ؼ��޾ƿ�
					}

					if (j == strlen(buffer) - 1)
					{
						semi_buffer[semi_line] = '\0';//������ ���� ����
						strcpy(token_table[token_line]->operand[op_num], semi_buffer); //operand�κп� ����
					}
				}
				//�м��� �������� �ʱ�ȭ
				memset(buffer, 0, strlen(buffer));//�迭�ʱ�ȭ
				buffer_line = 0;
			}
			else if (condition == 3)//comment�κп� ����
			{
				buffer[buffer_line] = '\0';//������ ���� ǥ�����ְ� �󺧺κκп� ����
				strcpy(token_table[token_line]->comment, buffer); //�̰��� �ڸ�Ʈ��
				memset(buffer, 0, strlen(buffer));//�迭�ʱ�ȭ
				buffer_line = 0;
			}
			if (str[i] == '\0')//����Ű�� ���� ���̻� Ž������ ����
			{
				break;
			}
			condition++;//������ �ٲ���
		}
		else//�׷��� �ʴٸ� ����ؼ� ���ڸ� �޾ƿ�
		{
			buffer[buffer_line++] = str[i];
		}
	}
	mark_line++;
	return 0;
}

/* ----------------------------------------------------------------------------------
 * ���� : �Է� ���ڿ��� ���� �ڵ������� �˻��ϴ� �Լ��̴�. 
 * �Ű� : ��ū ������ ���е� ���ڿ� 
 * ��ȯ : �������� = ���� ���̺� �ε���, ���� < 0 
 * ���� : 
 *		
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char *str) 
{
	int i;
	if (str[0] == '+')//'+'��ȣ�� 4������ ��Ÿ���� ���� ������ ��ɾ� �ν��� �Ҷ� �ʿ䰡 �������� ������� ����
	{
		for (i = 0; i <= (int)strlen(str); i++)//'+'���ڸ� ���ִ� �۾� 
		{
			str[i] = str[i + 1];
		}
	}
	for (i = 0; i < inst_index; i++)
	{
		if (strcmp(str, inst_table[i]->str) == 0) //���� �ڵ����� �˻� 
		{
			return i;//�´ٸ� �ش� �ε��� ���
		}
	}

	return -1;//ã�� ���Ͽ��ٸ� ���� ���

}

/* ----------------------------------------------------------------------------------
* ���� : ����� �ڵ带 ���� �н�1������ �����ϴ� �Լ��̴�.
*		   �н�1������..
*		   1. ���α׷� �ҽ��� ��ĵ�Ͽ� �ش��ϴ� ��ū������ �и��Ͽ� ���α׷� ���κ� ��ū
*		   ���̺��� �����Ѵ�.
*
* �Ű� : ����
* ��ȯ : ���� ���� = 0 , ���� = < 0
* ���� : ���� �ʱ� ���������� ������ ���� �˻縦 ���� �ʰ� �Ѿ �����̴�.
*	  ���� ������ ���� �˻� ��ƾ�� �߰��ؾ� �Ѵ�.
*
* -----------------------------------------------------------------------------------
*/
static int assem_pass1(void)
{
	int i,j;
	int errno;
	errno = 0;
	token_line = 0;//��ū ������ 0���� �ʱ�ȭ�� ���ش�.
	locctr = 0;//�ּ� ������ ���� ������ ������ �Լ�
	sym_len = 0;//sym_table�� ���̸� �ʱ�ȭ
	char check[20] = "";//���� �˻縦 �ϱ� ���� ���� �迭
	int op_index = 0;//��� ���� ������ ã�� ���� �۾�
	//int ltgcnt = 0;//LOTRG�κп��� �޸𸮸� ���ҋ� �̿��ϴ� ����
	ltg_len = 0;//LOTRG�κп��� �޸𸮸� ���ҋ� �̿��ϴ� ����
	char cpy_op[20];//���� ��ɾ �������ڷ� ���� ���ڿ� �迭
	if (init_pseudo_file("pseudo_20151812.txt") < 0) //����� ��ü ��ɾ ������ ���̺��� ����
	{
		printf("pseudo ������ �о���µ� �����Ͽ����ϴ�.\n");
	}

	if (sym_len >= MAX_LINES)//sym_table�� ũ�Ⱑ ���ġ�� �ʰ��ϸ� ����
	{
		errno = -1;
		return errno;
	}
		
	for (i = 0; i < line_num; i++)//�Է¹��� ���θ��� ��ū �з��� �� �ش�.
	{
		if (input_data[i][0] != '.')//'.'���� �����ϴ� �ּ� ������ ����� ���� �������� ��ū �з��� �� ���� �ʴ´�.
		{
			token_parsing(input_data[i]);//��ǲ ������ ������ ��ū �з��� �� �ش�.
			//������ ���� �˻縦 �ϴ� �κ�
			if (pseudo_check(token_table[token_line]->operator) == -1)//����� ��ü ��ɾ �ش����� ���� ��쿡�� ���
			{
				strcpy(check, token_table[token_line]->operator);//search_opcode�Լ� �Ķ���ͷ� ������ ���� token_table[token_line]->operator���ڿ� ����
				if (search_opcode(check) < 0)//���� ��Ͽ� ������ ���� ��ȯ
				{
					errno = -1;
					return errno;
				}
			}
			/*�ּҰ��� �������ִ� �۾�*/
	
			if (pseudo_check(token_table[token_line]->operator) != -1)//����� ��ü ��ɾ��� ���
			{

				if (strcmp(token_table[token_line]->operator, "START") == 0)//��ɾ START�̸� �ּ� ������ ������ �ش�
				{
					locctr = atoi(token_table[token_line]->operand[0]);
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
				}
				else if (strcmp(token_table[token_line]->operator,"CSECT") == 0)//��ɾ CSECT�̸� �ּ� ������ 0���� ������ �ش�
				{ 
					locctr = 0;//�ּ� ��ġ�� �ٽ� 0���� ����
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
				}
				else if (strcmp(token_table[token_line]->operator,"RESW") == 0)//��ɾ RESW�̸� operrand�� ���� 3�踦 �ּҹ��� ����
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
					locctr = locctr + (atoi(token_table[token_line]->operand[0]) * 3);
				}
				else if (strcmp(token_table[token_line]->operator,"RESB") == 0)//��ɾ RESB�̸� operrand�� ����ŭ �ּҹ��� ����
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
					locctr = locctr + atoi(token_table[token_line]->operand[0]);
				}
				else if (strcmp(token_table[token_line]->operator,"WORD") == 0)//��ɾ WORD�̸� �ּҸ� 3��ŭ ����
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
					locctr = locctr + 3;
				}
				else if (strcmp(token_table[token_line]->operator,"BYTE") == 0)//��ɾ WORD�̸� ��쿡 ���� �Ǻ�
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
					if (token_table[token_line]->operand[0][0] == 'C')//���� �ϳ��� 1����Ʈ�� ����
					{
						locctr += strlen(token_table[token_line]->operand[0]) - 3;
					}
					else if(token_table[token_line]->operand[0][0] == 'X')//�ƽ�Ű�ڵ� �ϳ��� 1����Ʈ�� ����
					{
						locctr += (strlen(token_table[token_line]->operand[0]) - 3) / 2;
					}
				}
				else if (strcmp(token_table[token_line]->operator,"EXTDEF") == 0)
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = -1; // -1�� �ּҰ� �������� �ʴ´ٴ� ���̴�.
				}
				else if (strcmp(token_table[token_line]->operator,"EXTREF") == 0)
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = -1; // -1�� �ּҰ� �������� �ʴ´ٴ� ���̴�.
				}
				else if (strcmp(token_table[token_line]->operator,"END") == 0)
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = -1; // -1�� �ּҰ� �������� �ʴ´ٴ� ���̴�.
					if (ltg_len != 0)
					{
						for (j = 0; j < ltg_len; j++)
						{
							sym_len++;
							strcpy(sym_table[sym_len].symbol, ltg_table[j].info);
							sym_table[sym_len].addr = locctr;
							locctr = locctr + ltg_table[j].size;
							token_line++;
							token_table[token_line] = (token*)malloc(sizeof(token));//��ū ���̺��� ���� �Ҵ����� �ϳ� �÷���
							strcpy(token_table[token_line]->label, ltg_table[j].info);
							strcpy(token_table[token_line]->operator,ltg_table[j].info);
							strcpy(token_table[token_line]->operand[0], "");
							strcpy(token_table[token_line]->operand[1], "");
							strcpy(token_table[token_line]->operand[2], "");
							strcpy(token_table[token_line]->comment, "");
							token_table[token_line]->nixbpe = 1;
						}
					}
				}
				else if (strcmp(token_table[token_line]->operator,"EQU") == 0)
				{
					if (strcmp(token_table[token_line]->operand[0], "*") == 0)//'*�̸� ���� �ּҸ� ����'
					{
						strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
						sym_table[sym_len].addr = locctr; 
					}
					else
					{
						int temp1, temp2;
						for (j = 0; j < sym_len; j++)
						{
							if (strcmp(sym_table[j].symbol, token_table[token_line]->operand[0]) == 0)
							{
								temp1 = sym_table[j].addr;
							}
							else if (strcmp(sym_table[j].symbol, token_table[token_line]->operand[1]) == 0)
							{
								temp2 = sym_table[j].addr;
							}
						}
						strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
						sym_table[sym_len].addr = temp1 - temp2;
					}
				}
				else if (strcmp(token_table[token_line]->operator,"LTORG") == 0) 
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = -1;
					for (j = 0; j < ltg_len; j++)
					{
						sym_len++;
						strcpy(sym_table[sym_len].symbol, ltg_table[j].info);
						sym_table[sym_len].addr = locctr;
						locctr = locctr + ltg_table[j].size;

						//��ū ���̺��� �ϳ� �÷��ִ� �۾�
						token_line++;
						token_table[token_line] = (token*)malloc(sizeof(token));//��ū ���̺��� ���� �Ҵ����� �ϳ� �÷���
						strcpy(token_table[token_line]->label, ltg_table[j].info);
						strcpy(token_table[token_line]->operator,ltg_table[j].info);
						strcpy(token_table[token_line]->operand[0],"");
						strcpy(token_table[token_line]->operand[1],"");
						strcpy(token_table[token_line]->operand[2],"");
						strcpy(token_table[token_line]->comment,"");
						token_table[token_line]->nixbpe = 1;
						mark_line++;
						
					}
					ltg_len = 0;//�ٽ� �ʱ�ȭ
				
				}
			}
			else
			{
				strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
				sym_table[sym_len].addr = locctr;
				//�ּҰ��� �÷��ִ� �۾�
				strcpy(cpy_op, token_table[token_line]->operator);
				op_index = search_opcode(cpy_op);
				if (inst_table[op_index]->format == 2)//2�����϶�
				{
					locctr = locctr + 2;
				}
				else if (inst_table[op_index]->format == 3)//3/4�����϶�
				{
					if (token_table[token_line]->operator[0] == '+')//�̶��� 4�����̴�.
					{
						locctr = locctr + 4;
					}
					else//�׷��� �ʴٸ� 3�����̴�
					{
						locctr = locctr + 3;
					}
				}

				//LOTORG�κ��� ����Ҷ� �̿�
				if (token_table[token_line]->operand[0][0] == '=')
				{
					int copy = 0;

					if (token_table[token_line]->operand[0][1] == 'C')
					{
						//ltgcnt = strlen(token_table[token_line]->operand[0]) - 4;
						//���߿� LTORG��ɾ �������� ó���ϱ� ���� �۾�
						for (j = 0; j < ltg_len; j++)
						{
							if (strcmp(ltg_table[j].info, token_table[token_line]->operand[0]) == 0)
							{
								copy = 1;
							}
						}
						if (copy != 1)//�ߺ��� ���� ������ �־���
						{
							strcpy(ltg_table[ltg_len].info, token_table[token_line]->operand[0]);
							ltg_table[ltg_len].size = strlen(token_table[token_line]->operand[0]) - 4;
							ltg_len++;
						}

					}
					else if (token_table[token_line]->operand[0][1] == 'X')
					{
						//ltgcnt = (strlen(token_table[token_line]->operand[0]) - 4) / 2;
						//���߿� LTORG��ɾ �������� ó���ϱ� ���� �۾�

						for (j = 0; j < ltg_len; j++)
						{
							if (strcmp(ltg_table[j].info, token_table[token_line]->operand[0]) == 0)
							{
								copy = 1;
							}
						}
						if (copy != 1)//�ߺ��� ���� ������ �־���
						{
							strcpy(ltg_table[ltg_len].info, token_table[token_line]->operand[0]);
							ltg_table[ltg_len].size = (strlen(token_table[token_line]->operand[0]) - 4) / 2;
							ltg_len++;
						}
					}
				}

			}
			sym_len++;
			token_line++;//��ū ������ �ϳ� �߰����ش�.
		}
	}

	return errno;//���� ����

}


/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ ��ɾ� ���� OPCODE�� ��ϵ� ǥ(���� 4��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*        ���� ���� 4�������� ���̴� �Լ��̹Ƿ� ������ ������Ʈ������ ������ �ʴ´�.
* -----------------------------------------------------------------------------------
*/
void make_opcode_output(char *file_name)
{
	FILE *fp;
	fp = fopen(file_name, "w");
	int i,j;
	int check;
	char check_op[20];
	char buff[3];//op�ڵ带 �޾ƿ��� ���� ����
	for (i = 0; i < token_line; i++)//��ū ���̺��� ������ŭ �ۼ�
	{
		fprintf(fp,token_table[i]->label);//label�κ� �ۼ�
		fprintf(fp,"\t");
		fprintf(fp,token_table[i]->operator);//operator�κ� �ۼ�
		fprintf(fp,"\t");
		fprintf(fp, token_table[i]->operand[0]);//operand�κ� �ۼ�
		for (j = 1; j <= 2; j++) //�������̸� �׸�ŭ �߰������� �ۼ�
		{
			if (strlen(token_table[i]->operand[j]) != 0)
			{
				fprintf(fp, ",");
				fprintf(fp, token_table[i]->operand[j]);
			}
		}
		fprintf(fp, "\t\t");
		strcpy(check_op, token_table[i]->operator);//search_opcode�Լ� �Ķ���ͷ� ������ ���� token_table[i]->operator���ڿ� ����
		check = search_opcode(check_op);//���� �ڵ����� �˻�
		if (check != -1)//���� �ڵ��̸� ���� �ڵ带 �ۼ��ϴ� �۾��� ����
		{
			char a = inst_table[check]->op / 16;
			char b = inst_table[check]->op % 16;

			if (a < 10)
			{
				buff[0] = 48 + a;
			}
			else
			{
				buff[0] = 55 + a;
			}

			if (b < 10)
			{
				buff[1] = 48 + b;
			}
			else
			{
				buff[1] = 55 + b;
			}

			buff[2] = '\0';

			fprintf(fp,buff);
			fprintf(fp,"\n");
		}
		else
		{
			fprintf(fp,"\n");
		}
	}
	fclose(fp);

}


/*----------------------------------------------------------------------------------
* ���� : �߰� �Լ� �κ��̴�. ������ �о�ͼ� ������� ��ü ��ɾ ���� ������ pseudo_table�� ����
* ��ȯ : �������� = 0 , ���� < 0 
* ---------------------------------------------------------------------------------- -
*/
int init_pseudo_file(char *pseudo_file)
{
	FILE *file;
	char buffer;
	char str_buff[20];//����� ��ü ��ɾ��� ���̴� 20�� ���� �ʴ´ٰ� ����
	int buff_len = 0;
	pseudo_index = 0;
	int errno;
	if ((file = fopen(pseudo_file, "r")) == NULL)
	{
		errno = -1;
		return errno;
	}
	else
	{
		errno = 0;
	}

	buffer = fgetc(file);
	while (buffer != EOF)
	{
		if (buffer == '\n')
		{
			str_buff[buff_len] = '\0';
			if (pseudo_index >= MAX_INST)
			{
				errno = -1;
				return -1;
			}
			pseudo_table[pseudo_index] = (char *)malloc(sizeof(char) * 20);//�޸𸮸� �����Ҵ�
			strcpy(pseudo_table[pseudo_index], str_buff);//����� ��ü ��ɾ ����
			pseudo_index++;//���̺��� ������ �÷���
			buff_len = 0;//�ʱ�ȭ
			memset(str_buff, 0, sizeof(str_buff));//�迭 �ʱ�ȭ
		}
		else
		{
			str_buff[buff_len++] = buffer;//����ؼ� ���ڸ� �����ؼ� �޾ƿ�
		}
		buffer = fgetc(file);//���Ͽ��� ���ڸ� �޾ƿ�
	}
	return errno;
}
/*----------------------------------------------------------------------------------
* ���� : �߰� �Լ� �κ��̴�. ���۷����� �κ��� ����� ��ü ��ɾ����� �˻縦 �� �ִ� �Լ�
* ��ȯ : ���ԵǸ� �ش� �ε���, ���Ե��� ������ -1�� ���
* ---------------------------------------------------------------------------------- -
*/

int pseudo_check(char *str)
{
	int i;
	for (i = 0; i < pseudo_index; i++)//pseudo ���̺� ���� �ϳ��� ����
	{
		if (strcmp(str, pseudo_table[i]) == 0)//������ �ش� �ε��� ���
		{
			return i;
		}
	}
	return -1;
}



/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ SYMBOL�� �ּҰ��� ����� TABLE�̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char *file_name)
{
	FILE *fp;
	fp = fopen(file_name, "w");
	int i;
	char temp[5];
	for (i = 0; i < sym_len; i++)
	{
		if ((strcmp(sym_table[i].symbol, "") != 0) && (sym_table[i].symbol[0] != '='))//symbol�κ��� ������� �ʴٸ�,Ȥ�� ���ͷ��� �ƴ϶�� ����Ѵ�
		{
			if((strcmp(sym_table[i].symbol, "RDREC") == 0) || ((strcmp(sym_table[i].symbol, "WRREC") == 0))) //RDREC �κ� Ȥ�� WDREC�κ��� ������ ��ĭ�� ���� ����Ѵ�
			{
				fprintf(fp, "\n");
			}
			fprintf(fp,"\t");
			fprintf(fp, sym_table[i].symbol);
			fprintf(fp, "\t\t");
			put_char_four(sym_table[i].addr, temp);
			fprintf(fp, temp);
			fprintf(fp, "\n");
			memset(temp, 0, sizeof(temp));
		}
	}
	fclose(fp);
}

/*----------------------------------------------------------------------------------
* ���� : �߰� �Լ� �κ��̴�. ������ 16���� ���ڿ� �迭�� �ٲ��ִ� �Լ�(4�ڸ�)
* �Ű� : �ٲ��� ������ ���ڿ��� ���� ����
* ---------------------------------------------------------------------------------- -
*/
void put_char_four(int num, char *str)
{
	int hex1, hex2, hex3,hex4;
	int len = 0;
	int check = 0;
	hex1 = num / (16*16*16);
	num = num - (hex1*(16 * 16 * 16));
	hex2 = num / (16*16);
	num = num - (hex2*(16 * 16 ));
	hex3 = num / 16;
	num = num - (hex3*16);
	hex4 = num % 16;

	/*���ڿ��� �ٲٴ� �۾�*/
	if (hex1 >= 10)
	{
		str[len++] = hex1 + 55;
		check = 1;
	}
	else if((1 <= hex1) && (hex1 < 10))
	{
		str[len++] = hex1 + 48;
		check = 1;
	}

	if (check == 1)
	{
		if (hex2 >= 10)
		{
			str[len++] = hex2 + 55;
		}
		else if ((0 <= hex2) && (hex2 < 10))
		{
			str[len++] = hex2 + 48;
		}
	}
	else if (check == 0)
	{
		if (hex2 >= 10)
		{
			str[len++] = hex2 + 55;
			check = 1;
		}
		else if ((1 <= hex2) && (hex2 < 10))
		{
			str[len++] = hex2 + 48;
			check = 1;
		}
	}


	if (check == 1)
	{
		if (hex3 >= 10)
		{
			str[len++] = hex3 + 55;
		}
		else if ((0 <= hex3) && (hex3 < 10))
		{
			str[len++] = hex3 + 48;
		}
	}
	else if (check == 0)
	{
		if (hex3 >= 10)
		{
			str[len++] = hex3 + 55;
			check = 1;
		}
		else if ((1 <= hex3) && (hex3 < 10))
		{
			str[len++] = hex3 + 48;
			check = 1;
		}
	}

	if (hex4 >= 10)
	{
		str[len++] = hex4 + 55;
	}
	else if ((0 <= hex4) && (hex4 < 10))
	{
		str[len++] = hex4 + 48;
	}

	str[len] = '\0';

	
}

/* ----------------------------------------------------------------------------------
* ���� : ����� �ڵ带 ���� �ڵ�� �ٲٱ� ���� �н�2 ������ �����ϴ� �Լ��̴�.
*		   �н� 2������ ���α׷��� ����� �ٲٴ� �۾��� ���� ������ ����ȴ�.
*		   ������ ���� �۾��� ����Ǿ� ����.
*		   1. ������ �ش� ����� ��ɾ ����� �ٲٴ� �۾��� �����Ѵ�.
* �Ű� : ����
* ��ȯ : �������� = 0, �����߻� = < 0
* ���� :
* -----------------------------------------------------------------------------------
*/
static int assem_pass2(void)
{
	
	int i,j;
	int errno;
	errno = 0;
	int index_inst = 0;
	int check = 0;
	char str[20] ="";
	int ref_check = 0;
	int section = 0;//���� ��� �������� ǥ���ϱ� ���� ����
	for (i = 0; i < token_line; i++)//��ū �и� �Ȱ͸��� ��� ����
	{
		if (cnt_line > MAX_LINES)
		{
			errno = -1;
			return errno;
		}
		opline[cnt_line] = (char  *)malloc(sizeof(char) * 10);//ũ�⸦ 10���� �����
		//���۷����Ϳ� ���� ���� �ε����� ã�ƿ�
		strcpy(str, token_table[i]->operator);
		index_inst = search_opcode(str);
		if (strcmp(token_table[i]->operator,"START") == 0)//START�̸� ����� �ٲ��� ����
		{
			opline[cnt_line++][0] = '\0';//���� �ڵ尡 �������� ����
			continue;
		}
		else if (strcmp(token_table[i]->operator,"EXTDEF") == 0)//��ɾ EXTDEF�� ���
		{
			def_table[def_line] = (char *)malloc(sizeof(char) * 10);
			strcpy(def_table[def_line++], token_table[i]->operand[0]);
			if (strcmp(token_table[i]->operand[1], "") != 0)//������� �ʴٸ� ���̺��� �־���
			{
				def_table[def_line] = (char *)malloc(sizeof(char) * 10);
				strcpy(def_table[def_line++], token_table[i]->operand[1]);
			}
			else if (strcmp(token_table[i]->operand[2], "") != 0)//������� �ʴٸ� ���̺��� �־���
			{
				def_table[def_line] = (char *)malloc(sizeof(char) * 10);
				strcpy(def_table[def_line++], token_table[i]->operand[2]);
			}
			opline[cnt_line++][0] = '\0';//���� �ڵ尡 �������� ����
			continue;
		}
		else if (strcmp(token_table[i]->operator,"EXTREF") == 0)//��ɾ EXTREF�� ���
		{
			if (ref_check == 0)//COPY���α׷��� ���� ���۷��� ���̺� ����
			{
				ref_table1[ref_line1] = (char *)malloc(sizeof(char) * 10);
				strcpy(ref_table1[ref_line1++], token_table[i]->operand[0]);
				if (strcmp(token_table[i]->operand[1], "") != 0)
				{
					ref_table1[ref_line1] = (char *)malloc(sizeof(char) * 10);
					strcpy(ref_table1[ref_line1++], token_table[i]->operand[1]);
				}
				if (strcmp(token_table[i]->operand[2], "") != 0)
				{
					ref_table1[ref_line1] = (char *)malloc(sizeof(char) * 10);
					strcpy(ref_table1[ref_line1++], token_table[i]->operand[2]);
				}
				ref_check++;
			}
			else if (ref_check == 1)//RDREC���α׷��� ���� ���۷��� ���̺� ����
			{
				ref_table2[ref_line2] = (char *)malloc(sizeof(char) * 10);
				strcpy(ref_table2[ref_line2++], token_table[i]->operand[0]);
				if (strcmp(token_table[i]->operand[1], "") != 0)
				{
					ref_table2[ref_line2] = (char *)malloc(sizeof(char) * 10);
					strcpy(ref_table2[ref_line2++], token_table[i]->operand[1]);
				}
				if (strcmp(token_table[i]->operand[2], "") != 0)
				{
					ref_table2[ref_line2] = (char *)malloc(sizeof(char) * 10);
					strcpy(ref_table2[ref_line2++], token_table[i]->operand[2]);
				}
				ref_check++;
			}
			else if (ref_check == 2)//WRREC���α׷��� ���� ���۷��� ���̺� ����
			{
				ref_table3[ref_line3] = (char *)malloc(sizeof(char) * 10);
				strcpy(ref_table3[ref_line3++], token_table[i]->operand[0]);
				if (strcmp(token_table[i]->operand[1], "") != 0)
				{
					ref_table3[ref_line3] = (char *)malloc(sizeof(char) * 10);
					strcpy(ref_table3[ref_line3++], token_table[i]->operand[1]);
				}
				if (strcmp(token_table[i]->operand[2], "") != 0)
				{
					ref_table3[ref_line3] = (char *)malloc(sizeof(char) * 10);
					strcpy(ref_table3[ref_line3++], token_table[i]->operand[2]);
				}
				ref_check++;
			}
			opline[cnt_line++][0] = '\0';//���� �ڵ尡 �������� ����
			continue;
		}
		else if (strcmp(token_table[i]->operator,"RESW") == 0)//��ɾ RESW�� ���
		{
			opline[cnt_line++][0] = '\0';//���� �ڵ尡 �������� ����
			continue;
		}
		else if (strcmp(token_table[i]->operator,"RESB") == 0)//��ɾ RESB�� ���
		{
			opline[cnt_line++][0] = '\0';//���� �ڵ尡 �������� ����
			continue;
		}
		else if (strcmp(token_table[i]->operator,"BYTE") == 0)//��ɾ BYTE�� ���
		{
			if (token_table[i]->operand[0][0] = 'X')
			{
				opline[cnt_line][0] = token_table[i]->operand[0][2];
				opline[cnt_line][1] = token_table[i]->operand[0][3];
				opline[cnt_line++][2] = '\0';
			}
			continue;
		}
		else if (strcmp(token_table[i]->operator,"WORD") == 0)//��ɾ WORD�� ���
		{
			
			int res;
			if ((strlen(token_table[i]->operand[0]) >= 48) && (strlen(token_table[i]->operand[0]) <= 57))//�ǿ����ڰ� ������ ���
			{
				res = atoi(token_table[i]->operand[0]);
				put_str_six(res, opline[cnt_line]);//10������ 16���� 6�ڸ���
				cnt_line++;
			}
			else
			{
				if (search_reference(section, token_table[i]->operand[0]) == 1)//���� ���۷��� ���̺� �ִٸ� �ְ��� 0���� ����
				{
					put_str_six(0, opline[cnt_line]);//10������ 16���� 6�ڸ���
					cnt_line++;
				}
				else
				{
					int idx = search_symbol(section, token_table[i]->operand[0]);
					res = sym_table[idx].addr;
					put_str_six(res, opline[cnt_line]);//10������ 16���� 6�ڸ���
					cnt_line++;

				}
			}
			continue;
		}
		else if (strcmp(token_table[i]->operator,"END") == 0)//��ɾ END�� ���
		{
			opline[cnt_line++][0] = '\0';//���� �ڵ尡 �������� ����
			continue;
		}
		else if (strcmp(token_table[i]->operator,"LTORG") == 0)//��ɾ LTORG�� ���
		{
			opline[cnt_line++][0] = '\0';//���� �ڵ尡 �������� ����
			continue;
		}
		else if (strcmp(token_table[i]->operator,"CSECT") == 0)//��ɾ CSECT�� ���
		{
			opline[cnt_line++][0] = '\0';//���� �ڵ尡 �������� ����
			section++;
			continue;
		}
		else if (strcmp(token_table[i]->operator,"EQU") == 0)//��ɾ EQU�� ���
		{
			opline[cnt_line++][0] = '\0';//���� �ڵ尡 �������� ����
			continue;
		}
		else if (token_table[i]->operator[0] == '=')
		{
			int temp_len = strlen(token_table[i]->operator);
			if (token_table[i]->operator[1] == 'X')//�̷��� ���� �ڵ带 �־���
			{
				opline[cnt_line][0] = token_table[i]->operator[3];
				opline[cnt_line][1] = token_table[i]->operator[4];
				opline[cnt_line][2] = '\0';
			}
			else if (token_table[i]->operator[1] == 'C')//���� �ڵ尪�� �־���
			{
				int c = 0;
				for (j = 3; j < (signed int)strlen(token_table[i]->operator) - 1; j++)
				{
					int t ;
					int result = 4 * 16;
					unsigned char t1;
					unsigned char t2;
					t = token_table[i]->operator[j];
					t -= 64;
					result = result + t;
					t1 = result / 16;
					t2 = result % 16;
					if ((t1 >= 0) && (t1 < 10))//�̷��� ���ڸ� �־���
					{
						opline[cnt_line][c++] = t1 + 48;
					}
					else//�̷��� ���ڸ� �־���
					{
						opline[cnt_line][c++] = t1 + 55;
					}

					if ((t2 >= 0) && (t2 < 10))//�̷��� ���ڸ� �־���
					{
						opline[cnt_line][c++] = t2 + 48;
					}
					else//�̷��� ���ڸ� �־���
					{
						opline[cnt_line][c++] = t2 + 55;
					}	
				}
				opline[cnt_line++][c] = '\0';
			}
			continue;
		}
		else
		{
			int index;
			int format;
			unsigned char opcode;
			unsigned char c1;//ù��° �� ����
			unsigned char c2;//�ι�° �� ����
			unsigned char c3;//����° �� ����
			unsigned char c4;//�׹�° �� ����
			unsigned char c5;//�׹�° �� ����

			index = search_opcode(str);
			opcode = (int)inst_table[index]->op;
			format = inst_table[index]->format;
			c1 = opcode / 16;
			c2 = opcode % 16;
			/*������ �ڵ带 �־��ִ� �۾�*/
			/*2���� ����*/
			if (format == 2)
			{
				//0��°
				if ((c1 >= 0) && (c1 < 10))//���ںκ�
				{

					opline[cnt_line][0] = c1 + 48;
				}
				else if (c1 >= 10)//���ںκ�
				{
					opline[cnt_line][0] = c1 + 55;
				}
				//ù��°
				if ((c2 >= 0) && (c2 < 10))//���ںκ�
				{

					opline[cnt_line][1] = c2 + 48;
				}
				else if (c2 >= 10)//���ںκ�
				{
					opline[cnt_line][1] = c2 + 55;
				}
				//�ι�°
				if (strcmp(token_table[i]->operand[0], "") != 0)
				{
					if (strcmp(token_table[i]->operand[0], "A") == 0)
					{
						opline[cnt_line][2] = A + 48;
					}
					else if (strcmp(token_table[i]->operand[0], "X") == 0)
					{
						opline[cnt_line][2] = X + 48;
					}
					else if (strcmp(token_table[i]->operand[0], "S") == 0)
					{
						opline[cnt_line][2] = S + 48;
					}
					else if (strcmp(token_table[i]->operand[0], "T") == 0)
					{
						opline[cnt_line][2] = T + 48;
					}

				}
				else
				{
					opline[cnt_line][2] = 48;
				}
				//����°
				if (strcmp(token_table[i]->operand[1], "") != 0)
				{
					if (strcmp(token_table[i]->operand[1], "A") == 0)
					{
						opline[cnt_line][3] = A + 48;
					}
					else if (strcmp(token_table[i]->operand[1], "X") == 0)
					{
						opline[cnt_line][3] = X + 48;
					}
					else if (strcmp(token_table[i]->operand[1], "S") == 0)
					{
						opline[cnt_line][3] = S + 48;
					}
					else if (strcmp(token_table[i]->operand[1], "T") == 0)
					{
						opline[cnt_line][3] = T + 48;
					}
				}
				else
				{
					opline[cnt_line][3] = 48;
				}
				//������ �� ����
				opline[cnt_line][4] = '\0';
				

			}
			/*3���� ����*/
			else if (token_table[i]->operator[0] != '+')//3�����϶�
			{


				//0��°
				if ((c1 >= 0) && (c1 < 10))//���ںκ�
				{

					opline[cnt_line][0] = c1 + 48;
				}
				else if (c1 >= 10)//���ںκ�
				{
					opline[cnt_line][0] = c1 + 55;
				}

				//ù��°
				if (token_table[i]->operand[0][0] == '#')//�̷��� immediate
				{
					c2 = c2 + 1;
					token_table[i]->nixbpe = 16;

				}
				else if (token_table[i]->operand[0][0] == '@')//�̷��� indirect
				{
					c2 = c2 + 2;
					token_table[i]->nixbpe = 32;
				}
				else//�̷��� simple
				{
					c2 = c2 + 3;
					token_table[i]->nixbpe = 32 + 16;
				}
				if ((c2 >= 0) && (c2 < 10))//���ںκ�
				{

					opline[cnt_line][1] = c2 + 48;
				}
				else if (c2 >= 10)//���ںκ�
				{
					opline[cnt_line][1] = c2 + 55;
				}
				

				//�ּ��� ���̸� ã�� �۾�
				//�ּ��� ���̸� ã�� �۾�
				int search_addr;
				int target;
				int pc;
				int res;
				if (strcmp(token_table[i]->operator,"RSUB") == 0)//RSUB�϶�
				{
					res = 0;
					opline[cnt_line][2] = 48; //xbpe��Ʈ ���� ��� 0
				}
				else
				{
					if (token_table[i]->operand[0][0] == '#')//�̷��� immediate
					{
						char cp[20];
						char *t_ary = filter_str(token_table[i]->operand[0]);
						strcpy(cp, t_ary);//�޸� ����
						free(t_ary);
						res = atoi(cp);
					}
					else if (token_table[i]->operand[0][0] == '@')//�̷��� indirect
					{
						char cp[20];
						char *t_ary = filter_str(token_table[i]->operand[0]);
						strcpy(cp, t_ary);
						free(t_ary);//�޸� ����
						search_addr = search_symbol(section,cp);
						target = sym_table[search_addr].addr;
						pc = sym_table[i].addr + 3; //3������ ���� ���
						res = target - pc;
					}
					else//�̷��� simple
					{
						search_addr = search_symbol(section,token_table[i]->operand[0]);
						target = sym_table[search_addr].addr;
						pc = sym_table[i].addr + 3; //3������ ���� ���
						res = target - pc;
					}



					if (res < 0)//�ּ��� ���� �����̸� �ٲ���� ��
					{
						int tempres = 0;//���̳ʽ��� �پ������� �����ϱ� ���� �Ű�����
						unsigned char t1;
						unsigned char t2;
						unsigned char t3;

						res = -res;

						t1 = res / (16 * 16);
						res -= t1 * (16 * 16);
						t2 = res / 16;
						res -= t2 * 16;
						t3 = res;

						tempres += (15 - t1) * (16 * 16);
						tempres += (15 - t2) * 16;
						tempres += (16 - t3);
						res = tempres;

					}



					//�ι�°
					int temp = 0;
					if (strcmp(token_table[i]->operand[1], "X") == 0)
					{
						temp = 8;
						token_table[i]->nixbpe += 8;
					}

					if ((res < 4096) && (token_table[i]->operand[0][0] != '#'))//���̰� 4096�� ���� �ʴ´ٸ� ���� immediate�� �ƴ� ��� pc relative 
					{
						temp += 2;
						token_table[i]->nixbpe += 2;
					}
					else if ((res > 4096) && (token_table[i]->operand[0][0] != '#'))//���̰� 4096�� �Ѵ´ٸ� ���� immediate�� �ƴ� ��� base relative 
					{
						temp += 4;
						token_table[i]->nixbpe += 4;
					}
					if ((temp >= 0) && (temp < 10))//���ںκ�
					{

						opline[cnt_line][2] = temp + 48;
					}
					else if (temp >= 10)//���ںκ�
					{
						opline[cnt_line][2] = temp + 55;
					}
				}
				//����° �׹�° �ټ���°�� �ּ��� ���� ���̸� �̿��Ͽ� �־���
				c3 = res / (16 * 16);
				c4 = (res - (c3 * 16 * 16)) / 16;
				c5 = res - ((c3 * 16 * 16) + (c4 * 16));
				if ((c3 >= 0) && (c3 < 10))//���ںκ�
				{

					opline[cnt_line][3] = c3 + 48;
				}
				else if (c3 >= 10)//���ںκ�
				{
					opline[cnt_line][3] = c3 + 55;
				}

				if ((c4 >= 0) && (c4 < 10))//���ںκ�
				{

					opline[cnt_line][4] = c4 + 48;
				}
				else if (c4 >= 10)//���ںκ�
				{
					opline[cnt_line][4] = c4 + 55;
				}

				if ((c5 >= 0) && (c5 < 10))//���ںκ�
				{

					opline[cnt_line][5] = c5 + 48;
				}
				else if (c5 >= 10)//���ںκ�
				{
					opline[cnt_line][5] = c5 + 55;
				}

				opline[cnt_line][6] = '\0';//������ ���� ����
			}
			/*4���� ����*/
			else if (token_table[i]->operator[0] == '+')//4�����϶�
			{


				//0��°
				if ((c1 >= 0) && (c1 < 10))//���ںκ�
				{

					opline[cnt_line][0] = c1 + 48;
				}
				else if (c1 >= 10)//���ںκ�
				{
					opline[cnt_line][0] = c1 + 55;
				}

				//ù��°
				if (token_table[i]->operand[0][0] == '#')//�̷��� immediate
				{
					c2 = c2 + 1;
					token_table[i]->nixbpe = 16;

				}
				else if (token_table[i]->operand[0][0] == '@')//�̷��� indirect
				{
					c2 = c2 + 2;
					token_table[i]->nixbpe = 32;
				}
				else//�̷��� simple
				{
					c2 = c2 + 3;
					token_table[i]->nixbpe = 32 + 16;
				}
				if ((c2 >= 0) && (c2 < 10))//���ںκ�
				{

					opline[cnt_line][1] = c2 + 48;
				}
				else if (c2 >= 10)//���ںκ�
				{
					opline[cnt_line][1] = c2 + 55;
				}


				//��ǥ�ּҸ� ã�� ���
				int search_addr;
				int res;
				if (search_reference(section, token_table[i]->operand[0]) == 1)
				{
					res = 0;
				}
				else
				{
					search_addr = search_symbol(section,token_table[i]->operand[0]);
					res = sym_table[search_addr].addr;
				}


				//�ι�°
				int temp = 0;
				if (strcmp(token_table[i]->operand[1], "X") == 0)
				{
					temp = 8;
					token_table[i]->nixbpe += 8;
				}


				//b, p�� ���δ� 0�̰� Ȯ�� ��Ʈ e�� 1�̴�
				temp += 1;
				token_table[i]->nixbpe += 1;
				if ((temp >= 0) && (temp < 10))//���ںκ�
				{

					opline[cnt_line][2] = temp + 48;
				}
				else if (temp >= 10)//���ںκ�
				{
					opline[cnt_line][2] = temp + 55;
				}

				//����° �׹�° �ټ���°�� �ּ��� ���� ���̸� �̿��Ͽ� �־���
				c1 = res / (16 * 16 * 16 * 16);
				res = res - (c1 * (16 * 16 * 16 * 16));
				c2 = res / 16 * 16 * 16;
				res = res - (c2*(16 * 16 * 16));
				c3 = res / 16 * 16;
				res = res - (c3*(16 * 16));
				c4 = res / 16;
				res = res - (c4 * 16);
				c5 = res;
				if ((c1 >= 0) && (c1 < 10))//���ںκ�
				{

					opline[cnt_line][3] = c1 + 48;
				}
				else if (c1 >= 10)//���ںκ�
				{
					opline[cnt_line][3] = c1 + 55;
				}

				if ((c2 >= 0) && (c2 < 10))//���ںκ�
				{

					opline[cnt_line][4] = c2 + 48;
				}
				else if (c2 >= 10)//���ںκ�
				{
					opline[cnt_line][4] = c2 + 55;
				}

				if ((c3 >= 0) && (c3 < 10))//���ںκ�
				{

					opline[cnt_line][5] = c3 + 48;
				}
				else if (c1 >= 10)//���ںκ�
				{
					opline[cnt_line][5] = c3 + 55;
				}

				if ((c4 >= 0) && (c4 < 10))//���ںκ�
				{

					opline[cnt_line][6] = c4 + 48;
				}
				else if (c4 >= 10)//���ںκ�
				{
					opline[cnt_line][6] = c4 + 55;
				}

				if ((c5 >= 0) && (c5 < 10))//���ںκ�
				{

					opline[cnt_line][7] = c5 + 48;
				}
				else if (c5 >= 10)//���ںκ�
				{
					opline[cnt_line][7] = c5 + 55;
				}

				opline[cnt_line][8] = '\0';//������ ���� ����

			}
		}

		cnt_line++;
	}

	return errno;

}

/*----------------------------------------------------------------------------------
* ���� : �߰� �Լ� �κ��̴�. �ɺ��� �ּҸ� ã���ִ� �Լ�
* �Ű� : ã�� �ɺ�
* ��ȯ�� : �������� = �ش� �ε���, �ַ� �߻� < 0
* ---------------------------------------------------------------------------------- -
*/

int search_symbol(int section,char *str)
{
	int i;
	int res = -1;
	int count= 0;
	for (i = 0; i < sym_len; i++)
	{
		if (strcmp(str, sym_table[i].symbol) == 0)//�ɺ��� ã������ �ش� �ε����� ��ȯ
		{
			res = i;
			count++;
			if (count > section)
			{
				break;
			}
		}
	}
	return res;
}


/*----------------------------------------------------------------------------------
* ���� : �߰� �Լ� �κ��̴�. #��ȣ�� @��ȣ�� �����ؼ� ���ڿ��� ����
* �Ű� : �ǿ�����
* ��ȯ : ������ ���ڿ�
* ---------------------------------------------------------------------------------- -
*/
char *filter_str(char *str)
{
	int i;
	char *res = (char *)malloc(sizeof(char) * 20);
	for (i = 0; i < (signed int)strlen(str); i++)//�ϳ��� ������ ����
	{
		res[i] = str[i + 1];
	}
	return res;
}

/*----------------------------------------------------------------------------------
* ���� : �߰� �Լ� �κ��̴�. ���۷��� ���̺��� �����Ͽ� �ش� ���ڿ��� �ִ��� �˻�
* �Ű� : �ǿ�����
* ��ȯ : ���� = 1 , ���� = 0
* ---------------------------------------------------------------------------------- -
*/
int search_reference(int section, char *str)
{
	int i;
	if(section == 0)//copy���α׷��� ���
	{
		for (i = 0; i < ref_line1; i++)
		{
			if (strcmp(str, ref_table1[i]) == 0)//�ִ� ���
			{
				return 1;
			}
		}
		return 0;//���� ���
	}
	else if(section == 1)//RDREC���α׷��� ���
	{
		for (i = 0; i < ref_line2; i++)
		{
			if (strcmp(str, ref_table2[i]) == 0)//�ִ� ���
			{
				return 1;
			}
		}
		return 0;
	}
	else if (section == 2)//WRREC���α׷��� ���
	{
		for (i = 0; i < ref_line3; i++)
		{
			if (strcmp(str, ref_table3[i]) == 0)//�ִ� ���
			{
				return 1;
			}
		}
		return 0;//���� ���
	}
	else//�ش� ������ input���Ͽ� ���� ������ ��� ���� ��ȯ
	{
		return 0;
	}
}

/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ object code (������Ʈ 1��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char *file_name)
{
	FILE *fp;
	fp = fopen(file_name, "w");
	int copy_len;
	int rd_len;
	int wd_len;
	int flag = 0;
	int i,j;
	int p;
	char buffer[10];
	char text[61] = "";
	char t_adr[10];
	char t_len[3];
	int section = 0;
	int ad;//���� ������ ���� ����
	int t_length = 0;//text���� ������ ���� ����
	int check;
	int start_adr;
	unsigned char c1;
	unsigned char c2;
	/*���α׷��� ���̸� �����ϴ� �۾�*/
	for (i = 0; i < token_line; i++)//�� �۾��� �ּ� ���̸� ����
	{
		if ((strcmp(token_table[i]->operator,"CSECT") == 0) && (flag == 0))//COPY���α׷� ���� ���ϱ�
		{
			copy_len = sym_table[i-2].addr;
			p = strlen(opline[i - 2]) / 2;
			copy_len += p;
			flag++;
		}
		else if ((strcmp(token_table[i]->operator,"CSECT") == 0) && (flag == 1))//RDREC���α׷� ���� ���ϱ�
		{
			rd_len = sym_table[i - 1].addr;
			p = strlen(opline[i - 1]) / 2;
			rd_len += p;
			flag++;
		}
	}
	wd_len = sym_table[i - 1].addr;//WDREC���α׷� ���� ���ϱ�
	p = strlen(opline[i - 1]) / 2;
	wd_len += p;

	flag = 0;


	for (i = 0; i < token_line; i++)
	{
		if (strcmp(token_table[i]->operator,"START") == 0)//���α׷��l ������ �˸��� �κ��� ó��
		{
			fprintf(fp, "H");//����κ� ���� ����
			fprintf(fp, token_table[i]->label);
			fprintf(fp, "\t");
			ad = atoi(token_table[i]->operand[0]);
			start_adr = ad;//���� �ּҸ� ����
			put_str_six(ad, buffer);
			fprintf(fp, buffer);
			ad = copy_len;
			put_str_six(ad, buffer);
			fprintf(fp, buffer);
			fprintf(fp, "\n");
		}
		else if (strcmp(token_table[i]->operator,"EXTDEF") == 0)//�ٸ� ���ǿ��� ������ �ϴ� �������� ����
		{
			fprintf(fp, "D");
			int count = 0;
			while ((strcmp(token_table[i]->operand[count], "") != 0) && (count <= 2))
			{
				strcpy(buffer, token_table[i]->operand[count]);
				fprintf(fp, token_table[i]->operand[count]);
				int idx = search_symbol(section, token_table[i]->operand[count]);
				ad = sym_table[idx].addr;
				put_str_six(ad, buffer);
				fprintf(fp, buffer);
				count++;
			}
			fprintf(fp, "\n");
		}
		else if (strcmp(token_table[i]->operator,"EXTREF") == 0)//�����ϴ� �������� ����
		{
			fprintf(fp, "R");
			if (section == 0)
			{
				for (j = 0; j < ref_line1; j++)
				{
					strcpy(buffer, ref_table1[j]);
					fprintf(fp, buffer);
					fprintf(fp, "\t");
				}
				fprintf(fp, "\n");
			}
			else if(section == 1)
			{
				for (j = 0; j < ref_line2; j++)
				{
					strcpy(buffer, ref_table2[j]);
					fprintf(fp, buffer);
				}
				fprintf(fp, "\n");
			}
			else if (section == 2)
			{
				for (j = 0; j < ref_line3; j++)
				{
					strcpy(buffer, ref_table3[j]);
					fprintf(fp, buffer);
				}
				fprintf(fp, "\n");
			}
		}
		else if(strcmp(token_table[i]->operator,"CSECT") == 0)//��Ʈ�� ������ ���� 
		{
		
			//ó������ ���� text�κ��� ���� ó��
			if (strlen(text) != 0)
			{
				fprintf(fp, "T");
				fprintf(fp, t_adr);
				c1 = t_length / 16;
				c2 = t_length % 16;
				if (c1 >= 0 && c1 < 10)
				{
					t_len[0] = c1 + 48;
				}
				else
				{
					t_len[0] = c1 + 55;
				}

				if (c2 >= 0 && c2 < 10)
				{
					t_len[1] = c2 + 48;
				}
				else
				{
					t_len[1] = c2 + 55;
				}
				t_len[2] = '\0';
				fprintf(fp, t_len);
				fprintf(fp, text);
				fprintf(fp, "\n");
				strcpy(text, "");

				strcpy(t_len, "");
				strcpy(t_adr, "");
				t_length = 0;

			}

			//���ġ�� ���� ������ ����
			if (section == 0)
			{
				for (j = 0; j < t1_line; j++)
				{
					if (strcmp(t1[j].opra, "WORD") != 0)//WORD�� �ƴϸ� �ǿ����� �״�� 
					{
						fprintf(fp, "M");
						t1[j].adr += 1;
						put_str_six(t1[j].adr, buffer);
						fprintf(fp, buffer);
						fprintf(fp, "05+");
						fprintf(fp, t1[j].name);
						fprintf(fp, "\n");
					}
					else if (strcmp(t2[j].opra, "WORD") == 0)//WORD�̰� �����ڰ� ���̿� ������ ����� �����͸� Ȱ���Ͽ� ó��
					{
						fprintf(fp, "M");
						put_str_six(t2[j].adr, buffer);
						fprintf(fp, buffer);
						fprintf(fp, "06+");
						fprintf(fp, t2[j].name);
						fprintf(fp, "\n");
						j++;
						fprintf(fp, "M");
						put_str_six(t2[j].adr, buffer);
						fprintf(fp, buffer);
						fprintf(fp, "06");
						buffer[0] = t2[j].mark[0];
						buffer[1] = '\0';
						fprintf(fp, buffer);
						fprintf(fp, t2[j].name);
						fprintf(fp, "\n");
					}
				}
			}
			else if (section == 1)//RDREC���ǵ� ���������� ó�� 
			{
				for (j = 0; j < t2_line; j++)
				{
					if (strcmp(t2[j].opra, "WORD") != 0)
					{
						fprintf(fp, "M");
						t2[j].adr += 1;
						put_str_six(t2[j].adr, buffer);
						fprintf(fp, buffer);
						fprintf(fp, "05+");
						fprintf(fp, t2[j].name);
						fprintf(fp, "\n");
					}
					else if(strcmp(t2[j].opra, "WORD") == 0)
					{
						fprintf(fp, "M");
						put_str_six(t2[j].adr, buffer);
						fprintf(fp, buffer);
						fprintf(fp, "06+");
						fprintf(fp, t2[j].name);
						fprintf(fp, "\n");
						j++;
						fprintf(fp, "M");
						put_str_six(t2[j].adr, buffer);
						fprintf(fp, buffer);
						fprintf(fp, "06");
						buffer[0] = t2[j].mark[0];
						buffer[1] = '\0';
						fprintf(fp, buffer);
						fprintf(fp, t2[j].name);
						fprintf(fp, "\n");
					}
					
				}
			}


			//End record
			if (section == 0)
			{
				fprintf(fp, "E");
				put_str_six(start_adr, buffer);
				fprintf(fp, buffer);
				fprintf(fp, "\n");

			}
			else if (section == 1)
			{
				fprintf(fp, "E\n");
			}

			if (section == 0)//RDREC�� ����
			{
				fprintf(fp, "\n");
				fprintf(fp, "H");//����κ� ���� ����
				fprintf(fp, token_table[i]->label);
				fprintf(fp, "\t");
				put_str_six(0, buffer);
				fprintf(fp, buffer);
				ad = rd_len;
				put_str_six(ad, buffer);
				fprintf(fp, buffer);
				fprintf(fp, "\n");
				section++;
			}
			else if (section == 1)
			{
				fprintf(fp, "\n");
				fprintf(fp, "H");//����κ� ���� ����
				fprintf(fp, token_table[i]->label);
				fprintf(fp, "\t");
				put_str_six(0, buffer);
				fprintf(fp, buffer);
				ad = wd_len;
				put_str_six(ad, buffer);
				fprintf(fp, buffer);
				fprintf(fp, "\n");
				section++;
			}
		}
		else//����Ǿ� �ִ� �ǿ����ڰ� �����Ǵ� ������ Ȯ���ϴ� �۾�
		{
			if (search_reference(section, token_table[i]->operand[0]) != 0)
			{
				if (section == 0)
				{
					strcpy(t1[t1_line].name, token_table[i]->operand[0]);
					strcpy(t1[t1_line].opra, token_table[i]->operator);
					t1[t1_line++].adr = sym_table[i].addr;
				}
				else if (section == 1)
				{
					strcpy(t2[t2_line].name, token_table[i]->operand[0]);
					strcpy(t2[t2_line].opra, token_table[i]->operator);
					t2[t2_line++].adr = sym_table[i].addr;
				}
				else if (section == 2)
				{
					strcpy(t3[t3_line].name, token_table[i]->operand[0]);
					strcpy(t3[t3_line].opra, token_table[i]->operator);
					t3[t3_line++].adr = sym_table[i].addr;
				}
			}

			if (search_reference(section, token_table[i]->operand[1]) != 0)
			{
				if (section == 0)
				{
					strcpy(t1[t1_line].name, token_table[i]->operand[1]);
					strcpy(t1[t1_line].opra, token_table[i]->operator);
					t1[t1_line].mark[0] = operand_mark[i][0];
					t1[t1_line++].adr = sym_table[i].addr;
				}
				else if (section == 1)
				{
					strcpy(t2[t2_line].name, token_table[i]->operand[1]);
					strcpy(t2[t2_line].opra, token_table[i]->operator);
					t2[t2_line].mark[0] = operand_mark[i][0];
					t2[t2_line++].adr = sym_table[i].addr;
				}
				else if (section == 2)
				{
					strcpy(t3[t3_line].name, token_table[i]->operand[1]);
					strcpy(t3[t3_line].opra, token_table[i]->operator);
					t2[t2_line].mark[0] = operand_mark[i][0];
					t3[t3_line++].adr = sym_table[i].addr;
				}
			}

			if (search_reference(section, token_table[i]->operand[2]) != 0)
			{
				if (section == 0)
				{
					strcpy(t1[t1_line].name, token_table[i]->operand[2]);
					strcpy(t1[t1_line].opra, token_table[i]->operator);
					t1[t1_line].mark[1] = operand_mark[i][1];
					t1[t1_line++].adr = sym_table[i].addr;
				}
				else if (section == 1)
				{
					strcpy(t2[t2_line].name, token_table[i]->operand[2]);
					strcpy(t2[t2_line].opra, token_table[i]->operator);
					t2[t2_line].mark[1] = operand_mark[i][1];
					t2[t2_line++].adr = sym_table[i].addr;
				}
				else if (section == 2)
				{
					strcpy(t3[t3_line].name, token_table[i]->operand[2]);
					strcpy(t3[t3_line].opra, token_table[i]->operator);
					t2[t2_line].mark[1] = operand_mark[i][1];
					t3[t3_line++].adr = sym_table[i].addr;
				}
			}

			if (strlen(text) == 0)//���̰� 0�̸� ���ο� �ּ��� ����
			{
				put_str_six(sym_table[i].addr, t_adr);
				t_length += strlen(opline[i]) / 2;
				strcat(text, opline[i]);
			}
			else
			{
				check = t_length + strlen(opline[i]) / 2;
				if (check > 30)//�̷��� �и� ����
				{
					fprintf(fp, "T");
					fprintf(fp, t_adr);
					c1 = t_length / 16;
					c2 = t_length % 16;
					if (c1 >= 0 && c1 < 10)
					{
						t_len[0] = c1 + 48;
					}
					else
					{
						t_len[0] = c1 + 55;
					}

					if (c2 >= 0 && c2 < 10)
					{
						t_len[1] = c2 + 48;
					}
					else
					{
						t_len[1] = c2 + 55;
					}
					t_len[2] = '\0';
					fprintf(fp, t_len);
					fprintf(fp, text);
					fprintf(fp, "\n");
					strcpy(text, opline[i]);//���� �������� �ٽ� ����
					put_str_six(sym_table[i].addr, t_adr);
					strcpy(t_len, "");
					t_length = strlen(opline[i]) / 2;
				}
				else if ((strcmp(token_table[i]->operator,"RESW") == 0) || (strcmp(token_table[i]->operator,"RESB") == 0))//�޸� ������ ����ִ� ��ɾ ������ ������ 
				{
					fprintf(fp, "T");
					fprintf(fp, t_adr);
					c1 = t_length / 16;
					c2 = t_length % 16;
					if (c1 >= 0 && c1 < 10)
					{
						t_len[0] = c1 + 48;
					}
					else
					{
						t_len[0] = c1 + 55;
					}

					if (c2 >= 0 && c2 < 10)
					{
						t_len[1] = c2 + 48;
					}
					else
					{
						t_len[1] = c2 + 55;
					}
					t_len[2] = '\0';
					fprintf(fp, t_len);
					fprintf(fp, text);
					fprintf(fp, "\n");
					strcpy(text, "");
					strcpy(t_len, "");
					strcpy(t_adr, "");
					t_length = 0;

				}
				else
				{
					t_length += strlen(opline[i]) / 2;
					strcat(text, opline[i]);
				}
			}
		}
	}
	/*WDREC������ �κ��� ó��*/
	fprintf(fp, "T");
	fprintf(fp, t_adr);
	c1 = t_length / 16;
	c2 = t_length % 16;
	if (c1 >= 0 && c1 < 10)
	{
		t_len[0] = c1 + 48;
	}
	else
	{
		t_len[0] = c1 + 55;
	}

	if (c2 >= 0 && c2 < 10)
	{
		t_len[1] = c2 + 48;
	}
	else
	{
		t_len[1] = c2 + 55;
	}
	t_len[2] = '\0';
	fprintf(fp, t_len);
	fprintf(fp, text);
	fprintf(fp, "\n");

	for (i = 0; i < t3_line; i++)
	{		
		if (strcmp(t3[j].opra, "WORD") != 0)
		{
			fprintf(fp, "M");
			t3[i].adr += 1;
			put_str_six(t3[i].adr, buffer);
			fprintf(fp, buffer);
			fprintf(fp, "05+");
			fprintf(fp, t3[i].name);
			fprintf(fp, "\n");
		}
		else if (strcmp(t2[j].opra, "WORD") == 0)
		{
			fprintf(fp, "M");
			put_str_six(t2[j].adr, buffer);
			fprintf(fp, buffer);
			fprintf(fp, "06+");
			fprintf(fp, t2[j].name);
			fprintf(fp, "\n");
			j++;
			fprintf(fp, "M");
			put_str_six(t2[j].adr, buffer);
			fprintf(fp, buffer);
			fprintf(fp, "06");
			buffer[0] = t2[j].mark[0];
			buffer[1] = '\0';
			fprintf(fp, buffer);
			fprintf(fp, t2[j].name);
			fprintf(fp, "\n");
		}
	}
	fprintf(fp, "E");


	fclose(fp);

}

/*----------------------------------------------------------------------------------
* ���� : �߰� �Լ� �κ��̴�. 16������ ��ȭ���Ѽ� ���ڿ��� �־��ִ� �Լ�(6�ڸ�)
* �Ű� : 10���� ����, ���ڿ��� ���� ����
* ---------------------------------------------------------------------------------- -
*/
void put_str_six(int ad, char *str)
{
	unsigned char c1;
	unsigned char c2;
	unsigned char c3;
	unsigned char c4;
	unsigned char c5;
	unsigned char c6;

	/*10���� ���ڸ� 16���� 5�ڸ��� ����� �ִ� �۾�*/
	c1 = ad / (16 * 16 * 16 * 16 * 16);
	ad = ad - c1 * (16 * 16 * 16 * 16 * 16);
	c2 = ad / (16 * 16 * 16 * 16);
	ad = ad - c2 * (16 * 16 * 16 * 16);
	c3 = ad / (16 * 16 * 16);
	ad = ad - c3 * (16 * 16 * 16);
	c4 = ad / (16 * 16);
	ad = ad - c4 * (16 * 16);
	c5 = ad / 16;
	c6 = ad % 16;

	if (c1 >= 0 && c1 < 10)//�ƽ�Ű �ڵ尪�� �̿�
	{
		str[0] = c1 + 48;
	}
	else
	{
		str[0] = c1 + 55;
	}

	if (c2 >= 0 && c2 < 10)
	{
		str[1] = c2 + 48;
	}
	else
	{
		str[1] = c2 + 55;
	}
	
	if (c3 >= 0 && c3 < 10)
	{
		str[2] = c3 + 48;
	}
	else
	{
		str[2] = c3 + 55;
	}
	
	if (c4 >= 0 && c4 < 10)
	{
		str[3] = c4 + 48;
	}
	else
	{
		str[3] = c4 + 55;
	}
	
	if (c5 >= 0 && c5 < 10)
	{
		str[4] = c5 + 48;
	}
	else
	{
		str[4] = c5 + 55;
	}

	if (c6 >= 0 && c6 < 10)
	{
		str[5] = c6 + 48;
	}
	else
	{
		str[5] = c6 + 55;
	}
	str[6] = '\0';
}