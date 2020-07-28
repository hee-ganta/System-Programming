/* 
 * my_assembler 함수를 위한 변수 선언 및 매크로를 담고 있는 헤더 파일이다. 
 * 
 */
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

//추가부분(레지스터 번호)
#define X 1
#define A 0
#define S 4
#define T 5


/* 
 * instruction 목록 파일로 부터 정보를 받아와서 생성하는 구조체 변수이다.
 * 라인 별로 하나의 instruction을 저장한다.
 */
struct inst_unit {
	char str[10];
	unsigned char op;
	int format;
	int ops;
};
typedef struct inst_unit inst;
inst *inst_table[MAX_INST];
int inst_index;


/*
 * 추가부분 -> 어셈블러 자체 명령어를 저장할 배열을 선언
*/
char *pseudo_table[MAX_INST];
int pseudo_index;


/*
 * 어셈블리 할 소스코드를 입력받는 테이블이다. 라인 단위로 관리할 수 있다.
 */
char *input_data[MAX_LINES];
static int line_num;


/* 
 * 어셈블리 할 소스코드를 토큰단위로 관리하기 위한 구조체 변수이다.
 * operator는 renaming을 허용한다.
 * nixbpe는 8bit 중 하위 6개의 bit를 이용하여 n,i,x,b,p,e를 표시한다.
 */
struct token_unit {
	char label[20];
	char operator[20]; 
	char operand[MAX_OPERAND][20];
	char comment[100];
	char nixbpe; 
};

typedef struct token_unit token; 
token *token_table[MAX_LINES]; 
static int token_line;

/*
 * 심볼을 관리하는 구조체이다.
 * 심볼 테이블은 심볼 이름, 심볼의 위치로 구성된다.
 */
struct symbol_unit {
	char symbol[10];
	int addr;
};

typedef struct symbol_unit symbol;
symbol sym_table[MAX_LINES];

//추가 부분(token parsing 에서 사용)<피연산자 사이에 어떤 기호가 있는지 저장>
char operand_mark[MAX_LINES][2] = {0,};
int mark_line = 0;

//추가 부분(pass1에서 사용)
int sym_len; //sym_table의 길이를 저장

static int locctr;

//LTORG정보를 저장할 구조체
struct ltg_control {
	char info[20];
	int size;
};

typedef struct ltg_control ltg;
ltg ltg_table[MAX_LINES];
int ltg_len;

//추가 부분(pass2에서 사용)
char *opline[MAX_LINES];
int cnt_line = 0;

char *def_table[MAX_LINES];
int def_line = 0;

char *ref_table1[MAX_LINES];
int ref_line1 = 0;
char *ref_table2[MAX_LINES];
int ref_line2 = 0;
char *ref_table3[MAX_LINES];
int ref_line3 = 0;


//추가부분(output파일을 만들 때 사용)
//재배치에 대한 정보를 저장
typedef struct relocate {
	char name[20];//피연산자
	char opra[20];//명령어
	int adr;//주소
	char mark[2];
}relocate;

relocate t1[5];//최대 길이를 5로 설정(COPY프로그램 사용 용도)
int t1_line=0;

relocate t2[5];//최대 길이를 5로 설정(RDREC프로그램 사용 용도)
int t2_line=0;

relocate t3[5];//최대 길이를 5로 설정(WDREC프로그램 사용 용도)
int t3_line=0;

//--------------

static char *input_file;
static char *output_file;
int init_my_assembler(void);
int init_inst_file(char *inst_file);
int init_input_file(char *input_file);
int token_parsing(char *str);
int search_opcode(char *str);
static int assem_pass1(void);
void make_opcode_output(char *file_name);

//추가 함수
int pseudo_check(char *str);
int init_pseudo_file(char *input_file);
void put_char_four(int num, char *str);
int search_symbol(int section,char *str);
char *filter_str(char *str);
int search_reference(int section, char *str);
void put_str_six(int num, char *str);

//프로젝트 사용 함수
void make_symtab_output(char *file_name);
static int assem_pass2(void);
void make_objectcode_output(char *file_name);