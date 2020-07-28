/*
 * 화일명 : my_assembler_00000000.c 
 * 설  명 : 이 프로그램은 SIC/XE 머신을 위한 간단한 Assembler 프로그램의 메인루틴으로,
 * 입력된 파일의 코드 중, 명령어에 해당하는 OPCODE를 찾아 출력한다.
 * 파일 내에서 사용되는 문자열 "00000000"에는 자신의 학번을 기입한다.
 */

/*
 *
 * 프로그램의 헤더를 정의한다. 
 *
 */
#pragma warning (disable : 4996)
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

// 파일명의 "00000000"은 자신의 학번으로 변경할 것.
#include "my_assembler_20151812.h"

/* ----------------------------------------------------------------------------------
 * 설명 : 사용자로 부터 어셈블리 파일을 받아서 명령어의 OPCODE를 찾아 출력한다.
 * 매계 : 실행 파일, 어셈블리 파일 
 * 반환 : 성공 = 0, 실패 = < 0 
 * 주의 : 현재 어셈블리 프로그램의 리스트 파일을 생성하는 루틴은 만들지 않았다. 
 *		   또한 중간파일을 생성하지 않는다. 
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[]) 
{
	if(init_my_assembler()< 0)
	{
		printf("init_my_assembler: 프로그램 초기화에 실패 했습니다.\n"); 
		return -1 ; 
	}

	if(assem_pass1() < 0 ){
		printf("assem_pass1: 패스1 과정에서 실패하였습니다.  \n") ; 
		return -1 ; 
	}
	//make_opcode_output("output_20151812.txt");

	
	//프로젝트에서 사용되는 부분
	make_symtab_output("symtab_20151812.txt");
	if(assem_pass2() < 0 ){
		printf(" assem_pass2: 패스2 과정에서 실패하였습니다.  \n") ; 
		return -1 ; 
	}
	

	make_objectcode_output("output_20151812.txt") ; 
	
	return 0;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 함수이다. 
 * 매계 : 없음
 * 반환 : 정상종료 = 0 , 에러 발생 = -1
 * 주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기 
 *		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
 *		   구현하였다. 
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
 * 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을 
 *        생성하는 함수이다. 
 * 매계 : 기계어 목록 파일
 * 반환 : 정상종료 = 0 , 에러 < 0 
 * 주의 : 기계어 목록파일 형식은 자유롭게 구현한다. 예시는 다음과 같다.
 *	
 *	===============================================================================
 *		   | 이름 | 형식 | 기계어 코드 | 오퍼랜드의 갯수 | NULL|
 *	===============================================================================	   
 *		
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char *inst_file)
{
	FILE *file;
	int errno;
	char str_buffer[10];//임시로 문자를 누적해서 받아올 저장공간
	int buffer_len = 0;//str_buffer의 인넥스
	int condition = 0;//inst구조체의 어느 변수에 저장을 해 줄 것인지 판별
	inst_index = 0;
	char buffer;//파일에서 문자단위로 받아옴

	if ((file = fopen(inst_file, "r")) == NULL)
	{
		printf("inst파일을 읽는데 실패하였습니다\n");
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
		if ((buffer != '\t') && (buffer != '\n'))//탭키나  엔터키가 아니면 계속해서 문자열을 생성함
		{
			if (buffer != '\n')
			{
				str_buffer[buffer_len++] = buffer;
			}
			buffer = fgetc(file);
		}
		else if ((buffer == '\t') && (buffer != '\n'))//탭키이고 엔터키가 아닌 경우
		{
			str_buffer[buffer_len] = '\0';//문장의 끝을 만듬
			if (condition == 0)//명령어 이름 저장
			{
				if (inst_index >= MAX_INST)//더이상 담을 공간이 없으면 에러로 인식하고 errno를 리턴
				{
					errno = -1;
					return errno;
				}
				inst_table[inst_index] = (inst *)malloc(sizeof(inst)); //추가 테이블 동적할당을 해줌
				strcpy(inst_table[inst_index]->str, str_buffer);//복사를 해줌
				buffer_len = 0;//길이 리셋
				memset(str_buffer, 0, strlen(str_buffer));//배열초기화
			}
			else if (condition == 1)//명령어의 포멧 저장
			{
				if (strlen(str_buffer) != 1)//3/4형식이므로 편의상 3을 넣어줌
				{
					inst_table[inst_index]->format = 3;
				}
				else
				{
					inst_table[inst_index]->format = atoi(str_buffer);//정수로 변환을 하여 넣어줌
				}
				buffer_len = 0;//길이 리셋
				memset(str_buffer, 0, strlen(str_buffer));//배열초기화
			}
			else if (condition == 2)//명령어의 OPCODE저장
			{
				char temp1;
				char temp2;
				unsigned char temp= 0;
				temp1 = str_buffer[0];
				temp2 = str_buffer[1];
				/*두 문자를 전환*/
				//temp1은 16자리수
				if (temp1 >= 65) //알파벳인 경우 ,이때 16진수 인것도 같이 고려
				{
					temp = (temp1 - 55) * 16 + temp;
				}
				else
				{
					temp = (temp1 - 48) * 16 + temp;
				}
				//temp2는 1의자리수
				if (temp2 >= 65) //알파벳인 경우
				{
					temp =temp + (temp2 - 55);
				}
				else
				{
					temp =temp + (temp2 - 48);
				}
				inst_table[inst_index]->op = temp;//전환된 문자를 구조체 안에다가 넣어줌
				buffer_len = 0;//길이 리셋
				memset(str_buffer, 0, strlen(str_buffer));//배열초기화
			}
			/*공백 문자가 없을때까지 받아옴*/
			buffer = fgetc(file);
			while (buffer == '\t')
			{
				buffer = fgetc(file);
			}
			condition++;
		}
		else if (buffer == '\n')//엔터키를 받으면 라인의 끝
		{
			//마지막꺼 넣어주기
			str_buffer[buffer_len] = '\0';//문장의 끝을 세팅
			if (condition == 3)
			{
				inst_table[inst_index++]->ops = atoi(str_buffer);//버퍼에나 넣어주고 테이블 크기 증가
				buffer_len = 0;//길이 리셋
				memset(str_buffer, 0, strlen(str_buffer));//배열초기화
			}
			condition = 0;
			buffer = fgetc(file);
		}
	}
	fclose(file);
	return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 할 소스코드를 읽어 소스코드 테이블(input_data)를 생성하는 함수이다. 
 * 매계 : 어셈블리할 소스파일명
 * 반환 : 정상종료 = 0 , 에러 < 0  
 * 주의 : 라인단위로 저장한다.
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
		printf("input파일을 읽는데 실패하였습니다\n");
		errno = -1;
	}
	else
	{
		errno = 0;
	}

	fgets(check, 2000, file);//라인별로 문자열을 읽어들임

	while (strcmp(check,"") != 0)//파일에서 더이상 받아올게 없을 때까지 받아옴
	{
		if (line_num >= MAX_LINES)
		{
			errno = -1;
			return errno;
		}
		input_data[line_num] = (char*)malloc(2001 * sizeof(char));//저장 공간이 생성이 가능하다면 동적할당
		strcpy(input_data[line_num], check);//문자열을 넣어줌
		input_data[line_num][strlen(check)-1] = '\0';//문자열의 끝세팅
		memset(check, 0, strlen(check));//다시 받아올 문자열을 위해 임시 저장 공간 리셋
		line_num++;//input_data 갯수를 늘려줌
		fgets(check, 2000, file);//라인별로 문자열을 읽어들임
	}
	fclose(file);
	
	return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다. 
 *        패스 1로 부터 호출된다. 
 * 매계 : 파싱을 원하는 문자열  
 * 반환 : 정상종료 = 0 , 에러 < 0 
 * 주의 : my_assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다. 
 * ----------------------------------------------------------------------------------
 */
int token_parsing(char *str) 
{
	char buffer[110]="";//임시로 문자를 누적해서 받아올 저장공간
	int buffer_line=0;//str_buffer의 인덱스 설정
	char semi_buffer[30] = "";//오퍼랜드를 ','기준으로 쪼개어서 각각 저장하기 위해 받아올 문자열의 임시 공간
	int semi_line = 0;//semi_buffer의 인덱스
	int condition = 0;//token구조체의 어느 변수에 저장을 해 줄건지 구분하기 위한 변수
	int i,j;
	int errno;
	errno = 0;
	if (token_line >= MAX_LINES)
	{
		errno = -1;
		return errno;
	}
	token_table[token_line] = (token*)malloc(sizeof(token));//토큰 테이블을 동적 할당으로 하나 늘려줌
	/*공백으로 초기화*/
	strcpy(token_table[token_line]->label, "");
	strcpy(token_table[token_line]->operator, "");
	for (i = 0; i < MAX_OPERAND; i++)
	{
		strcpy(token_table[token_line]->operand[i], "");
	}
	strcpy(token_table[token_line]->comment, "");
	token_table[token_line]->nixbpe = 1; //nixbpe초기화
	for (i = 0; i <= (int)strlen(str); i++)//한 라인의 길이까지 고려
	{
		int mark_count = 0;
		if ((str[i] == '\t') || (str[i] == '\0') )//탭키이거나 문장의 끝일때
		{
			if (condition == 0)//label부분에 저장
			{
				buffer[buffer_line] = '\0';//문장의 끝을 표시해주고 라벨부부분에 복사
				strcpy(token_table[token_line]->label, buffer); //이것은 라벨임
				memset(buffer, 0, strlen(buffer));//배열초기화
				buffer_line = 0;
			}
			else if (condition == 1)//operator부분에 저장
			{
				buffer[buffer_line] = '\0';//문장의 끝을 표시해주고 라벨부부분에 복사
				strcpy(token_table[token_line]->operator, buffer); //이것은 오퍼레이터
				memset(buffer, 0, strlen(buffer));//배열초기화
				buffer_line = 0;
			}
			else if (condition == 2)//operand부분에 저장
			{
				buffer[buffer_line] = '\0';//문장의 끝을 표시
				int op_num = 0;
				for (j = 0; j < (int)strlen(buffer); j++)
				{
					if (buffer[j] == ',' || buffer[j] == '-')
					{
						semi_buffer[semi_line] = '\0';
						if (op_num >= MAX_OPERAND)//담을 수 있는 갯수가 넘어가면 에러부분이라 -1을 리턴
						{
							errno = -1;
							return errno;
						}
						strcpy(token_table[token_line]->operand[op_num++], semi_buffer); //operand부분에 저장
						memset(semi_buffer, 0, strlen(semi_buffer));//배열초기화
						semi_line = 0;//길이 초기화
						operand_mark[mark_line][mark_count++] = buffer[j];
						continue;
					}
					else
					{
						semi_buffer[semi_line++] = buffer[j];//구분지어지는 곳이 아니라면 계속해서받아옴
					}

					if (j == strlen(buffer) - 1)
					{
						semi_buffer[semi_line] = '\0';//문장의 끝을 세팅
						strcpy(token_table[token_line]->operand[op_num], semi_buffer); //operand부분에 저장
					}
				}
				//분석이 끝났으면 초기화
				memset(buffer, 0, strlen(buffer));//배열초기화
				buffer_line = 0;
			}
			else if (condition == 3)//comment부분에 저장
			{
				buffer[buffer_line] = '\0';//문장의 끝을 표시해주고 라벨부부분에 복사
				strcpy(token_table[token_line]->comment, buffer); //이것은 코멘트임
				memset(buffer, 0, strlen(buffer));//배열초기화
				buffer_line = 0;
			}
			if (str[i] == '\0')//엔터키가 오면 더이상 탐색하지 않음
			{
				break;
			}
			condition++;//조건을 바꿔줌
		}
		else//그렇지 않다면 계속해서 문자를 받아옴
		{
			buffer[buffer_line++] = str[i];
		}
	}
	mark_line++;
	return 0;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다. 
 * 매계 : 토큰 단위로 구분된 문자열 
 * 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0 
 * 주의 : 
 *		
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char *str) 
{
	int i;
	if (str[0] == '+')//'+'기호는 4형식을 나타낼떄 쓰고 실제로 명령어 인식을 할땐 필요가 없음으로 고려하지 않음
	{
		for (i = 0; i <= (int)strlen(str); i++)//'+'문자를 뺴주는 작업 
		{
			str[i] = str[i + 1];
		}
	}
	for (i = 0; i < inst_index; i++)
	{
		if (strcmp(str, inst_table[i]->str) == 0) //기계어 코드인지 검사 
		{
			return i;//맞다면 해당 인데스 출력
		}
	}

	return -1;//찾지 못하였다면 음수 출력

}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
*		   패스1에서는..
*		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
*		   테이블을 생성한다.
*
* 매계 : 없음
* 반환 : 정상 종료 = 0 , 에러 = < 0
* 주의 : 현재 초기 버전에서는 에러에 대한 검사를 하지 않고 넘어간 상태이다.
*	  따라서 에러에 대한 검사 루틴을 추가해야 한다.
*
* -----------------------------------------------------------------------------------
*/
static int assem_pass1(void)
{
	int i,j;
	int errno;
	errno = 0;
	token_line = 0;//토큰 라인을 0으로 초기화를 해준다.
	locctr = 0;//주소 번지에 대한 정보를 저장할 함수
	sym_len = 0;//sym_table의 길이를 초기화
	char check[20] = "";//에러 검사를 하기 위한 문자 배열
	int op_index = 0;//기계어에 대한 정보를 찾기 위한 작업
	//int ltgcnt = 0;//LOTRG부분에서 메모리를 셈할떄 이용하는 변수
	ltg_len = 0;//LOTRG부분에서 메모리를 셈할떄 이용하는 변수
	char cpy_op[20];//기계어 명령어에 전달인자로 보낼 문자열 배열
	if (init_pseudo_file("pseudo_20151812.txt") < 0) //어셈블리 자체 명령어를 저장할 테이블을 생성
	{
		printf("pseudo 파일을 읽어오는데 실패하였습니다.\n");
	}

	if (sym_len >= MAX_LINES)//sym_table의 크기가 허용치를 초과하면 에러
	{
		errno = -1;
		return errno;
	}
		
	for (i = 0; i < line_num; i++)//입력받은 라인마다 토큰 분류를 해 준다.
	{
		if (input_data[i][0] != '.')//'.'으로 시작하는 주석 라인은 출력을 하지 않음으로 토큰 분류를 해 주지 않는다.
		{
			token_parsing(input_data[i]);//인풋 데이터 한줄을 토큰 분류를 해 준다.
			//에러에 대한 검사를 하는 부분
			if (pseudo_check(token_table[token_line]->operator) == -1)//어셈블리 자체 명령어에 해당하지 않을 경우에만 고려
			{
				strcpy(check, token_table[token_line]->operator);//search_opcode함수 파라미터로 보내기 위한 token_table[token_line]->operator문자열 복사
				if (search_opcode(check) < 0)//기계어 목록에 없으면 에러 반환
				{
					errno = -1;
					return errno;
				}
			}
			/*주소값을 세팅해주는 작업*/
	
			if (pseudo_check(token_table[token_line]->operator) != -1)//어셈블리 자체 명령어일 경우
			{

				if (strcmp(token_table[token_line]->operator, "START") == 0)//명령어가 START이면 주소 번지를 세팅해 준다
				{
					locctr = atoi(token_table[token_line]->operand[0]);
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
				}
				else if (strcmp(token_table[token_line]->operator,"CSECT") == 0)//명령어가 CSECT이면 주소 번지를 0으로 세팅해 준다
				{ 
					locctr = 0;//주소 위치를 다시 0으로 세팅
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
				}
				else if (strcmp(token_table[token_line]->operator,"RESW") == 0)//명령어가 RESW이면 operrand의 수의 3배를 주소번지 증가
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
					locctr = locctr + (atoi(token_table[token_line]->operand[0]) * 3);
				}
				else if (strcmp(token_table[token_line]->operator,"RESB") == 0)//명령어가 RESB이면 operrand의 수만큼 주소번지 증가
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
					locctr = locctr + atoi(token_table[token_line]->operand[0]);
				}
				else if (strcmp(token_table[token_line]->operator,"WORD") == 0)//명령어가 WORD이면 주소를 3만큼 증가
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
					locctr = locctr + 3;
				}
				else if (strcmp(token_table[token_line]->operator,"BYTE") == 0)//명령어가 WORD이면 경우에 따라 판별
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = locctr;
					if (token_table[token_line]->operand[0][0] == 'C')//문자 하나당 1바이트씩 증가
					{
						locctr += strlen(token_table[token_line]->operand[0]) - 3;
					}
					else if(token_table[token_line]->operand[0][0] == 'X')//아스키코드 하나당 1바이트씩 증가
					{
						locctr += (strlen(token_table[token_line]->operand[0]) - 3) / 2;
					}
				}
				else if (strcmp(token_table[token_line]->operator,"EXTDEF") == 0)
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = -1; // -1은 주소가 존재하지 않는다는 뜻이다.
				}
				else if (strcmp(token_table[token_line]->operator,"EXTREF") == 0)
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = -1; // -1은 주소가 존재하지 않는다는 뜻이다.
				}
				else if (strcmp(token_table[token_line]->operator,"END") == 0)
				{
					strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
					sym_table[sym_len].addr = -1; // -1은 주소가 존재하지 않는다는 뜻이다.
					if (ltg_len != 0)
					{
						for (j = 0; j < ltg_len; j++)
						{
							sym_len++;
							strcpy(sym_table[sym_len].symbol, ltg_table[j].info);
							sym_table[sym_len].addr = locctr;
							locctr = locctr + ltg_table[j].size;
							token_line++;
							token_table[token_line] = (token*)malloc(sizeof(token));//토큰 테이블을 동적 할당으로 하나 늘려줌
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
					if (strcmp(token_table[token_line]->operand[0], "*") == 0)//'*이면 현재 주소를 저장'
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

						//토큰 테이블을 하나 늘려주는 작업
						token_line++;
						token_table[token_line] = (token*)malloc(sizeof(token));//토큰 테이블을 동적 할당으로 하나 늘려줌
						strcpy(token_table[token_line]->label, ltg_table[j].info);
						strcpy(token_table[token_line]->operator,ltg_table[j].info);
						strcpy(token_table[token_line]->operand[0],"");
						strcpy(token_table[token_line]->operand[1],"");
						strcpy(token_table[token_line]->operand[2],"");
						strcpy(token_table[token_line]->comment,"");
						token_table[token_line]->nixbpe = 1;
						mark_line++;
						
					}
					ltg_len = 0;//다시 초기화
				
				}
			}
			else
			{
				strcpy(sym_table[sym_len].symbol, token_table[token_line]->label);
				sym_table[sym_len].addr = locctr;
				//주소값을 늘려주는 작업
				strcpy(cpy_op, token_table[token_line]->operator);
				op_index = search_opcode(cpy_op);
				if (inst_table[op_index]->format == 2)//2형식일때
				{
					locctr = locctr + 2;
				}
				else if (inst_table[op_index]->format == 3)//3/4형식일때
				{
					if (token_table[token_line]->operator[0] == '+')//이때는 4형식이다.
					{
						locctr = locctr + 4;
					}
					else//그렇지 않다면 3형식이다
					{
						locctr = locctr + 3;
					}
				}

				//LOTORG부분을 계산할때 이용
				if (token_table[token_line]->operand[0][0] == '=')
				{
					int copy = 0;

					if (token_table[token_line]->operand[0][1] == 'C')
					{
						//ltgcnt = strlen(token_table[token_line]->operand[0]) - 4;
						//나중에 LTORG명령어를 만났을때 처리하기 위한 작업
						for (j = 0; j < ltg_len; j++)
						{
							if (strcmp(ltg_table[j].info, token_table[token_line]->operand[0]) == 0)
							{
								copy = 1;
							}
						}
						if (copy != 1)//중복이 되지 않으면 넣어줌
						{
							strcpy(ltg_table[ltg_len].info, token_table[token_line]->operand[0]);
							ltg_table[ltg_len].size = strlen(token_table[token_line]->operand[0]) - 4;
							ltg_len++;
						}

					}
					else if (token_table[token_line]->operand[0][1] == 'X')
					{
						//ltgcnt = (strlen(token_table[token_line]->operand[0]) - 4) / 2;
						//나중에 LTORG명령어를 만났을때 처리하기 위한 작업

						for (j = 0; j < ltg_len; j++)
						{
							if (strcmp(ltg_table[j].info, token_table[token_line]->operand[0]) == 0)
							{
								copy = 1;
							}
						}
						if (copy != 1)//중복이 되지 않으면 넣어줌
						{
							strcpy(ltg_table[ltg_len].info, token_table[token_line]->operand[0]);
							ltg_table[ltg_len].size = (strlen(token_table[token_line]->operand[0]) - 4) / 2;
							ltg_len++;
						}
					}
				}

			}
			sym_len++;
			token_line++;//토큰 갯수를 하나 추가해준다.
		}
	}

	return errno;//정상 종료

}


/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 명령어 옆에 OPCODE가 기록된 표(과제 4번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*        또한 과제 4번에서만 쓰이는 함수이므로 이후의 프로젝트에서는 사용되지 않는다.
* -----------------------------------------------------------------------------------
*/
void make_opcode_output(char *file_name)
{
	FILE *fp;
	fp = fopen(file_name, "w");
	int i,j;
	int check;
	char check_op[20];
	char buff[3];//op코드를 받아오기 위한 버퍼
	for (i = 0; i < token_line; i++)//토큰 테이블의 갯수만큼 작성
	{
		fprintf(fp,token_table[i]->label);//label부분 작성
		fprintf(fp,"\t");
		fprintf(fp,token_table[i]->operator);//operator부분 작성
		fprintf(fp,"\t");
		fprintf(fp, token_table[i]->operand[0]);//operand부분 작성
		for (j = 1; j <= 2; j++) //여러개이면 그만큼 추가적으로 작성
		{
			if (strlen(token_table[i]->operand[j]) != 0)
			{
				fprintf(fp, ",");
				fprintf(fp, token_table[i]->operand[j]);
			}
		}
		fprintf(fp, "\t\t");
		strcpy(check_op, token_table[i]->operator);//search_opcode함수 파라미터로 보내기 위한 token_table[i]->operator문자열 복사
		check = search_opcode(check_op);//기계어 코드인지 검사
		if (check != -1)//기계어 코드이면 기계어 코드를 작성하는 작업을 수행
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
* 설명 : 추가 함수 부분이다. 파일을 읽어와서 어셈블리어 자체 명령어에 대한 정보를 pseudo_table에 저장
* 반환 : 정상종료 = 0 , 에러 < 0 
* ---------------------------------------------------------------------------------- -
*/
int init_pseudo_file(char *pseudo_file)
{
	FILE *file;
	char buffer;
	char str_buff[20];//어셈블리 자체 명령어의 길이는 20이 넘지 않는다고 가정
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
			pseudo_table[pseudo_index] = (char *)malloc(sizeof(char) * 20);//메모리를 동적할당
			strcpy(pseudo_table[pseudo_index], str_buff);//어셈블리 자체 명령어를 저장
			pseudo_index++;//테이블의 갯수를 늘려줌
			buff_len = 0;//초기화
			memset(str_buff, 0, sizeof(str_buff));//배열 초기화
		}
		else
		{
			str_buff[buff_len++] = buffer;//계속해서 문자를 누적해서 받아옴
		}
		buffer = fgetc(file);//파일에서 문자를 받아옴
	}
	return errno;
}
/*----------------------------------------------------------------------------------
* 설명 : 추가 함수 부분이다. 오퍼레이터 부분이 어셈블러 자체 명령어인지 검사를 해 주는 함수
* 반환 : 포함되면 해당 인덱스, 포함되지 않으면 -1을 출력
* ---------------------------------------------------------------------------------- -
*/

int pseudo_check(char *str)
{
	int i;
	for (i = 0; i < pseudo_index; i++)//pseudo 테이블 전부 하나씩 조사
	{
		if (strcmp(str, pseudo_table[i]) == 0)//같으면 해당 인덱스 출력
		{
			return i;
		}
	}
	return -1;
}



/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 SYMBOL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
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
		if ((strcmp(sym_table[i].symbol, "") != 0) && (sym_table[i].symbol[0] != '='))//symbol부분이 비어있지 않다면,혹은 리터럴이 아니라면 출력한다
		{
			if((strcmp(sym_table[i].symbol, "RDREC") == 0) || ((strcmp(sym_table[i].symbol, "WRREC") == 0))) //RDREC 부분 혹은 WDREC부분이 나오면 한칸을 띄우고 출력한다
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
* 설명 : 추가 함수 부분이다. 정수를 16진수 문자열 배열로 바꿔주는 함수(4자리)
* 매계 : 바꿔줄 정수와 문자열을 담을 공간
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

	/*문자열로 바꾸는 작업*/
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
* 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
*		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
*		   다음과 같은 작업이 수행되어 진다.
*		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
* 매계 : 없음
* 반환 : 정상종료 = 0, 에러발생 = < 0
* 주의 :
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
	int section = 0;//현재 어느 섹션인지 표시하기 위한 변수
	for (i = 0; i < token_line; i++)//토큰 분리 된것마다 기계어를 정돈
	{
		if (cnt_line > MAX_LINES)
		{
			errno = -1;
			return errno;
		}
		opline[cnt_line] = (char  *)malloc(sizeof(char) * 10);//크기를 10개를 잡아줌
		//오퍼레이터에 대한 정보 인덱스를 찾아옴
		strcpy(str, token_table[i]->operator);
		index_inst = search_opcode(str);
		if (strcmp(token_table[i]->operator,"START") == 0)//START이면 기계어로 바꾸지 않음
		{
			opline[cnt_line++][0] = '\0';//기계어 코드가 존재하지 않음
			continue;
		}
		else if (strcmp(token_table[i]->operator,"EXTDEF") == 0)//명령어가 EXTDEF인 경우
		{
			def_table[def_line] = (char *)malloc(sizeof(char) * 10);
			strcpy(def_table[def_line++], token_table[i]->operand[0]);
			if (strcmp(token_table[i]->operand[1], "") != 0)//비어있지 않다면 레이블을 넣어줌
			{
				def_table[def_line] = (char *)malloc(sizeof(char) * 10);
				strcpy(def_table[def_line++], token_table[i]->operand[1]);
			}
			else if (strcmp(token_table[i]->operand[2], "") != 0)//비어있지 않다면 레이블을 넣어줌
			{
				def_table[def_line] = (char *)malloc(sizeof(char) * 10);
				strcpy(def_table[def_line++], token_table[i]->operand[2]);
			}
			opline[cnt_line++][0] = '\0';//기계어 코드가 존재하지 않음
			continue;
		}
		else if (strcmp(token_table[i]->operator,"EXTREF") == 0)//명령어가 EXTREF인 경우
		{
			if (ref_check == 0)//COPY프로그램에 대한 레퍼런스 테이블 세팅
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
			else if (ref_check == 1)//RDREC프로그램에 대한 레퍼런스 테이블 세팅
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
			else if (ref_check == 2)//WRREC프로그램에 대한 레퍼런스 테이블 세팅
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
			opline[cnt_line++][0] = '\0';//기계어 코드가 존재하지 않음
			continue;
		}
		else if (strcmp(token_table[i]->operator,"RESW") == 0)//명령어가 RESW인 경우
		{
			opline[cnt_line++][0] = '\0';//기계어 코드가 존재하지 않음
			continue;
		}
		else if (strcmp(token_table[i]->operator,"RESB") == 0)//명령어가 RESB인 경우
		{
			opline[cnt_line++][0] = '\0';//기계어 코드가 존재하지 않음
			continue;
		}
		else if (strcmp(token_table[i]->operator,"BYTE") == 0)//명령어가 BYTE인 경우
		{
			if (token_table[i]->operand[0][0] = 'X')
			{
				opline[cnt_line][0] = token_table[i]->operand[0][2];
				opline[cnt_line][1] = token_table[i]->operand[0][3];
				opline[cnt_line++][2] = '\0';
			}
			continue;
		}
		else if (strcmp(token_table[i]->operator,"WORD") == 0)//명령어가 WORD인 경우
		{
			
			int res;
			if ((strlen(token_table[i]->operand[0]) >= 48) && (strlen(token_table[i]->operand[0]) <= 57))//피연산자가 숫자인 경우
			{
				res = atoi(token_table[i]->operand[0]);
				put_str_six(res, opline[cnt_line]);//10진수를 16진수 6자리로
				cnt_line++;
			}
			else
			{
				if (search_reference(section, token_table[i]->operand[0]) == 1)//만약 레퍼런스 테이블에 있다면 주고값을 0으로 설정
				{
					put_str_six(0, opline[cnt_line]);//10진수를 16진수 6자리로
					cnt_line++;
				}
				else
				{
					int idx = search_symbol(section, token_table[i]->operand[0]);
					res = sym_table[idx].addr;
					put_str_six(res, opline[cnt_line]);//10진수를 16진수 6자리로
					cnt_line++;

				}
			}
			continue;
		}
		else if (strcmp(token_table[i]->operator,"END") == 0)//명령어가 END인 경우
		{
			opline[cnt_line++][0] = '\0';//기계어 코드가 존재하지 않음
			continue;
		}
		else if (strcmp(token_table[i]->operator,"LTORG") == 0)//명령어가 LTORG인 경우
		{
			opline[cnt_line++][0] = '\0';//기계어 코드가 존재하지 않음
			continue;
		}
		else if (strcmp(token_table[i]->operator,"CSECT") == 0)//명령어가 CSECT인 경우
		{
			opline[cnt_line++][0] = '\0';//기계어 코드가 존재하지 않음
			section++;
			continue;
		}
		else if (strcmp(token_table[i]->operator,"EQU") == 0)//명령어가 EQU인 경우
		{
			opline[cnt_line++][0] = '\0';//기계어 코드가 존재하지 않음
			continue;
		}
		else if (token_table[i]->operator[0] == '=')
		{
			int temp_len = strlen(token_table[i]->operator);
			if (token_table[i]->operator[1] == 'X')//이러면 기계어 코드를 넣어줌
			{
				opline[cnt_line][0] = token_table[i]->operator[3];
				opline[cnt_line][1] = token_table[i]->operator[4];
				opline[cnt_line][2] = '\0';
			}
			else if (token_table[i]->operator[1] == 'C')//문자 코드값을 넣어줌
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
					if ((t1 >= 0) && (t1 < 10))//이러면 숫자를 넣어줌
					{
						opline[cnt_line][c++] = t1 + 48;
					}
					else//이러면 문자를 넣어줌
					{
						opline[cnt_line][c++] = t1 + 55;
					}

					if ((t2 >= 0) && (t2 < 10))//이러면 숫자를 넣어줌
					{
						opline[cnt_line][c++] = t2 + 48;
					}
					else//이러면 문자를 넣어줌
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
			unsigned char c1;//첫번째 들어갈 정보
			unsigned char c2;//두번째 들어갈 정보
			unsigned char c3;//세번째 들어갈 정보
			unsigned char c4;//네번째 들어갈 정보
			unsigned char c5;//네번째 들어갈 정보

			index = search_opcode(str);
			opcode = (int)inst_table[index]->op;
			format = inst_table[index]->format;
			c1 = opcode / 16;
			c2 = opcode % 16;
			/*연산자 코드를 넣어주는 작업*/
			/*2형식 구간*/
			if (format == 2)
			{
				//0번째
				if ((c1 >= 0) && (c1 < 10))//숫자부분
				{

					opline[cnt_line][0] = c1 + 48;
				}
				else if (c1 >= 10)//문자부분
				{
					opline[cnt_line][0] = c1 + 55;
				}
				//첫번째
				if ((c2 >= 0) && (c2 < 10))//숫자부분
				{

					opline[cnt_line][1] = c2 + 48;
				}
				else if (c2 >= 10)//문자부분
				{
					opline[cnt_line][1] = c2 + 55;
				}
				//두번째
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
				//세번째
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
				//마지막 끝 선언
				opline[cnt_line][4] = '\0';
				

			}
			/*3형식 구간*/
			else if (token_table[i]->operator[0] != '+')//3형식일때
			{


				//0번째
				if ((c1 >= 0) && (c1 < 10))//숫자부분
				{

					opline[cnt_line][0] = c1 + 48;
				}
				else if (c1 >= 10)//문자부분
				{
					opline[cnt_line][0] = c1 + 55;
				}

				//첫번째
				if (token_table[i]->operand[0][0] == '#')//이러면 immediate
				{
					c2 = c2 + 1;
					token_table[i]->nixbpe = 16;

				}
				else if (token_table[i]->operand[0][0] == '@')//이러면 indirect
				{
					c2 = c2 + 2;
					token_table[i]->nixbpe = 32;
				}
				else//이러면 simple
				{
					c2 = c2 + 3;
					token_table[i]->nixbpe = 32 + 16;
				}
				if ((c2 >= 0) && (c2 < 10))//숫자부분
				{

					opline[cnt_line][1] = c2 + 48;
				}
				else if (c2 >= 10)//문자부분
				{
					opline[cnt_line][1] = c2 + 55;
				}
				

				//주소의 차이를 찾는 작업
				//주소의 차이를 찾는 작업
				int search_addr;
				int target;
				int pc;
				int res;
				if (strcmp(token_table[i]->operator,"RSUB") == 0)//RSUB일때
				{
					res = 0;
					opline[cnt_line][2] = 48; //xbpe비트 값은 모두 0
				}
				else
				{
					if (token_table[i]->operand[0][0] == '#')//이러면 immediate
					{
						char cp[20];
						char *t_ary = filter_str(token_table[i]->operand[0]);
						strcpy(cp, t_ary);//메모리 해제
						free(t_ary);
						res = atoi(cp);
					}
					else if (token_table[i]->operand[0][0] == '@')//이러면 indirect
					{
						char cp[20];
						char *t_ary = filter_str(token_table[i]->operand[0]);
						strcpy(cp, t_ary);
						free(t_ary);//메모리 해제
						search_addr = search_symbol(section,cp);
						target = sym_table[search_addr].addr;
						pc = sym_table[i].addr + 3; //3형식인 것을 고려
						res = target - pc;
					}
					else//이러면 simple
					{
						search_addr = search_symbol(section,token_table[i]->operand[0]);
						target = sym_table[search_addr].addr;
						pc = sym_table[i].addr + 3; //3형식인 것을 고려
						res = target - pc;
					}



					if (res < 0)//주소의 차가 음수이면 바꿔줘야 함
					{
						int tempres = 0;//마이너스가 붙었을때의 저리하기 위한 매개변수
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



					//두번째
					int temp = 0;
					if (strcmp(token_table[i]->operand[1], "X") == 0)
					{
						temp = 8;
						token_table[i]->nixbpe += 8;
					}

					if ((res < 4096) && (token_table[i]->operand[0][0] != '#'))//차이가 4096을 넘지 않는다면 또한 immediate가 아닐 경우 pc relative 
					{
						temp += 2;
						token_table[i]->nixbpe += 2;
					}
					else if ((res > 4096) && (token_table[i]->operand[0][0] != '#'))//차이가 4096을 넘는다면 또한 immediate가 아닐 경우 base relative 
					{
						temp += 4;
						token_table[i]->nixbpe += 4;
					}
					if ((temp >= 0) && (temp < 10))//숫자부분
					{

						opline[cnt_line][2] = temp + 48;
					}
					else if (temp >= 10)//문자부분
					{
						opline[cnt_line][2] = temp + 55;
					}
				}
				//세번째 네번째 다섯번째는 주소의 값의 차이를 이용하여 넣어줌
				c3 = res / (16 * 16);
				c4 = (res - (c3 * 16 * 16)) / 16;
				c5 = res - ((c3 * 16 * 16) + (c4 * 16));
				if ((c3 >= 0) && (c3 < 10))//숫자부분
				{

					opline[cnt_line][3] = c3 + 48;
				}
				else if (c3 >= 10)//문자부분
				{
					opline[cnt_line][3] = c3 + 55;
				}

				if ((c4 >= 0) && (c4 < 10))//숫자부분
				{

					opline[cnt_line][4] = c4 + 48;
				}
				else if (c4 >= 10)//문자부분
				{
					opline[cnt_line][4] = c4 + 55;
				}

				if ((c5 >= 0) && (c5 < 10))//숫자부분
				{

					opline[cnt_line][5] = c5 + 48;
				}
				else if (c5 >= 10)//문자부분
				{
					opline[cnt_line][5] = c5 + 55;
				}

				opline[cnt_line][6] = '\0';//문장의 끝을 선언
			}
			/*4형식 구간*/
			else if (token_table[i]->operator[0] == '+')//4형식일때
			{


				//0번째
				if ((c1 >= 0) && (c1 < 10))//숫자부분
				{

					opline[cnt_line][0] = c1 + 48;
				}
				else if (c1 >= 10)//문자부분
				{
					opline[cnt_line][0] = c1 + 55;
				}

				//첫번째
				if (token_table[i]->operand[0][0] == '#')//이러면 immediate
				{
					c2 = c2 + 1;
					token_table[i]->nixbpe = 16;

				}
				else if (token_table[i]->operand[0][0] == '@')//이러면 indirect
				{
					c2 = c2 + 2;
					token_table[i]->nixbpe = 32;
				}
				else//이러면 simple
				{
					c2 = c2 + 3;
					token_table[i]->nixbpe = 32 + 16;
				}
				if ((c2 >= 0) && (c2 < 10))//숫자부분
				{

					opline[cnt_line][1] = c2 + 48;
				}
				else if (c2 >= 10)//문자부분
				{
					opline[cnt_line][1] = c2 + 55;
				}


				//목표주소를 찾는 방법
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


				//두번째
				int temp = 0;
				if (strcmp(token_table[i]->operand[1], "X") == 0)
				{
					temp = 8;
					token_table[i]->nixbpe += 8;
				}


				//b, p는 전부다 0이고 확장 비트 e만 1이다
				temp += 1;
				token_table[i]->nixbpe += 1;
				if ((temp >= 0) && (temp < 10))//숫자부분
				{

					opline[cnt_line][2] = temp + 48;
				}
				else if (temp >= 10)//문자부분
				{
					opline[cnt_line][2] = temp + 55;
				}

				//세번째 네번째 다섯번째는 주소의 값의 차이를 이용하여 넣어줌
				c1 = res / (16 * 16 * 16 * 16);
				res = res - (c1 * (16 * 16 * 16 * 16));
				c2 = res / 16 * 16 * 16;
				res = res - (c2*(16 * 16 * 16));
				c3 = res / 16 * 16;
				res = res - (c3*(16 * 16));
				c4 = res / 16;
				res = res - (c4 * 16);
				c5 = res;
				if ((c1 >= 0) && (c1 < 10))//숫자부분
				{

					opline[cnt_line][3] = c1 + 48;
				}
				else if (c1 >= 10)//문자부분
				{
					opline[cnt_line][3] = c1 + 55;
				}

				if ((c2 >= 0) && (c2 < 10))//숫자부분
				{

					opline[cnt_line][4] = c2 + 48;
				}
				else if (c2 >= 10)//문자부분
				{
					opline[cnt_line][4] = c2 + 55;
				}

				if ((c3 >= 0) && (c3 < 10))//숫자부분
				{

					opline[cnt_line][5] = c3 + 48;
				}
				else if (c1 >= 10)//문자부분
				{
					opline[cnt_line][5] = c3 + 55;
				}

				if ((c4 >= 0) && (c4 < 10))//숫자부분
				{

					opline[cnt_line][6] = c4 + 48;
				}
				else if (c4 >= 10)//문자부분
				{
					opline[cnt_line][6] = c4 + 55;
				}

				if ((c5 >= 0) && (c5 < 10))//숫자부분
				{

					opline[cnt_line][7] = c5 + 48;
				}
				else if (c5 >= 10)//문자부분
				{
					opline[cnt_line][7] = c5 + 55;
				}

				opline[cnt_line][8] = '\0';//문장의 끝을 선언

			}
		}

		cnt_line++;
	}

	return errno;

}

/*----------------------------------------------------------------------------------
* 설명 : 추가 함수 부분이다. 심볼의 주소를 찾아주는 함수
* 매계 : 찾을 심볼
* 반환값 : 정상종료 = 해당 인덱스, 애러 발생 < 0
* ---------------------------------------------------------------------------------- -
*/

int search_symbol(int section,char *str)
{
	int i;
	int res = -1;
	int count= 0;
	for (i = 0; i < sym_len; i++)
	{
		if (strcmp(str, sym_table[i].symbol) == 0)//심볼을 찾앗으면 해당 인덱스를 반환
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
* 설명 : 추가 함수 부분이다. #기호나 @기호를 제거해서 문자열을 리턴
* 매계 : 피연산자
* 반환 : 수정된 문자열
* ---------------------------------------------------------------------------------- -
*/
char *filter_str(char *str)
{
	int i;
	char *res = (char *)malloc(sizeof(char) * 20);
	for (i = 0; i < (signed int)strlen(str); i++)//하나씩 앞으로 땡김
	{
		res[i] = str[i + 1];
	}
	return res;
}

/*----------------------------------------------------------------------------------
* 설명 : 추가 함수 부분이다. 레퍼런스 테이블을 참조하여 해당 문자열이 있는지 검사
* 매계 : 피연산자
* 반환 : 성공 = 1 , 실패 = 0
* ---------------------------------------------------------------------------------- -
*/
int search_reference(int section, char *str)
{
	int i;
	if(section == 0)//copy프로그램의 경우
	{
		for (i = 0; i < ref_line1; i++)
		{
			if (strcmp(str, ref_table1[i]) == 0)//있는 경우
			{
				return 1;
			}
		}
		return 0;//없는 경우
	}
	else if(section == 1)//RDREC프로그램의 경우
	{
		for (i = 0; i < ref_line2; i++)
		{
			if (strcmp(str, ref_table2[i]) == 0)//있는 경우
			{
				return 1;
			}
		}
		return 0;
	}
	else if (section == 2)//WRREC프로그램의 경우
	{
		for (i = 0; i < ref_line3; i++)
		{
			if (strcmp(str, ref_table3[i]) == 0)//있는 경우
			{
				return 1;
			}
		}
		return 0;//없는 경우
	}
	else//해당 섹션이 input파일에 없는 섹션일 경우 에러 반환
	{
		return 0;
	}
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 object code (프로젝트 1번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
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
	int ad;//길이 정보를 넣을 변수
	int t_length = 0;//text길이 정보를 넣을 변수
	int check;
	int start_adr;
	unsigned char c1;
	unsigned char c2;
	/*프로그램의 길이를 저장하는 작업*/
	for (i = 0; i < token_line; i++)//이 작업때 주소 길이를 저장
	{
		if ((strcmp(token_table[i]->operator,"CSECT") == 0) && (flag == 0))//COPY프로그램 길이 구하기
		{
			copy_len = sym_table[i-2].addr;
			p = strlen(opline[i - 2]) / 2;
			copy_len += p;
			flag++;
		}
		else if ((strcmp(token_table[i]->operator,"CSECT") == 0) && (flag == 1))//RDREC프로그램 길이 구하기
		{
			rd_len = sym_table[i - 1].addr;
			p = strlen(opline[i - 1]) / 2;
			rd_len += p;
			flag++;
		}
	}
	wd_len = sym_table[i - 1].addr;//WDREC프로그램 길이 구하기
	p = strlen(opline[i - 1]) / 2;
	wd_len += p;

	flag = 0;


	for (i = 0; i < token_line; i++)
	{
		if (strcmp(token_table[i]->operator,"START") == 0)//프로그램릐 시작을 알리는 부분을 처림
		{
			fprintf(fp, "H");//헤더부분 정보 저장
			fprintf(fp, token_table[i]->label);
			fprintf(fp, "\t");
			ad = atoi(token_table[i]->operand[0]);
			start_adr = ad;//시작 주소를 저장
			put_str_six(ad, buffer);
			fprintf(fp, buffer);
			ad = copy_len;
			put_str_six(ad, buffer);
			fprintf(fp, buffer);
			fprintf(fp, "\n");
		}
		else if (strcmp(token_table[i]->operator,"EXTDEF") == 0)//다른 섹션에서 쓰고자 하는 변수들을 나열
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
		else if (strcmp(token_table[i]->operator,"EXTREF") == 0)//참조하는 변수들을 나열
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
		else if(strcmp(token_table[i]->operator,"CSECT") == 0)//컨트롤 섹션을 나눔 
		{
		
			//처리하지 못한 text부분을 마저 처리
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

			//재배치에 대한 정보를 저장
			if (section == 0)
			{
				for (j = 0; j < t1_line; j++)
				{
					if (strcmp(t1[j].opra, "WORD") != 0)//WORD가 아니면 피연산자 그대로 
					{
						fprintf(fp, "M");
						t1[j].adr += 1;
						put_str_six(t1[j].adr, buffer);
						fprintf(fp, buffer);
						fprintf(fp, "05+");
						fprintf(fp, t1[j].name);
						fprintf(fp, "\n");
					}
					else if (strcmp(t2[j].opra, "WORD") == 0)//WORD이고 연산자가 사이에 있으면 저장된 데이터를 활용하여 처리
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
			else if (section == 1)//RDREC섹션도 마찬가지로 처리 
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

			if (section == 0)//RDREC의 시작
			{
				fprintf(fp, "\n");
				fprintf(fp, "H");//헤더부분 정보 저장
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
				fprintf(fp, "H");//헤더부분 정보 저장
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
		else//저장되어 있는 피연산자가 참조되는 것인지 확인하는 작업
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

			if (strlen(text) == 0)//길이가 0이면 새로운 주소의 시작
			{
				put_str_six(sym_table[i].addr, t_adr);
				t_length += strlen(opline[i]) / 2;
				strcat(text, opline[i]);
			}
			else
			{
				check = t_length + strlen(opline[i]) / 2;
				if (check > 30)//이러면 분리 시작
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
					strcpy(text, opline[i]);//현재 기준으로 다시 세팅
					put_str_six(sym_table[i].addr, t_adr);
					strcpy(t_len, "");
					t_length = strlen(opline[i]) / 2;
				}
				else if ((strcmp(token_table[i]->operator,"RESW") == 0) || (strcmp(token_table[i]->operator,"RESB") == 0))//메모리 공간을 잡아주는 명령어가 나오면 끊어줌 
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
	/*WDREC나머지 부분을 처리*/
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
* 설명 : 추가 함수 부분이다. 16진수로 변화시켜서 문자열에 넣어주는 함수(6자리)
* 매계 : 10진수 숫자, 문자열을 담을 공간
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

	/*10진수 숫자를 16진수 5자리로 만들어 주는 작업*/
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

	if (c1 >= 0 && c1 < 10)//아스키 코드값을 이용
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