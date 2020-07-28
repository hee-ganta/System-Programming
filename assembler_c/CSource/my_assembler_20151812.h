/* 
 * my_assembler �Լ��� ���� ���� ���� �� ��ũ�θ� ��� �ִ� ��� �����̴�. 
 * 
 */
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

//�߰��κ�(�������� ��ȣ)
#define X 1
#define A 0
#define S 4
#define T 5


/* 
 * instruction ��� ���Ϸ� ���� ������ �޾ƿͼ� �����ϴ� ����ü �����̴�.
 * ���� ���� �ϳ��� instruction�� �����Ѵ�.
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
 * �߰��κ� -> ����� ��ü ��ɾ ������ �迭�� ����
*/
char *pseudo_table[MAX_INST];
int pseudo_index;


/*
 * ����� �� �ҽ��ڵ带 �Է¹޴� ���̺��̴�. ���� ������ ������ �� �ִ�.
 */
char *input_data[MAX_LINES];
static int line_num;


/* 
 * ����� �� �ҽ��ڵ带 ��ū������ �����ϱ� ���� ����ü �����̴�.
 * operator�� renaming�� ����Ѵ�.
 * nixbpe�� 8bit �� ���� 6���� bit�� �̿��Ͽ� n,i,x,b,p,e�� ǥ���Ѵ�.
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
 * �ɺ��� �����ϴ� ����ü�̴�.
 * �ɺ� ���̺��� �ɺ� �̸�, �ɺ��� ��ġ�� �����ȴ�.
 */
struct symbol_unit {
	char symbol[10];
	int addr;
};

typedef struct symbol_unit symbol;
symbol sym_table[MAX_LINES];

//�߰� �κ�(token parsing ���� ���)<�ǿ����� ���̿� � ��ȣ�� �ִ��� ����>
char operand_mark[MAX_LINES][2] = {0,};
int mark_line = 0;

//�߰� �κ�(pass1���� ���)
int sym_len; //sym_table�� ���̸� ����

static int locctr;

//LTORG������ ������ ����ü
struct ltg_control {
	char info[20];
	int size;
};

typedef struct ltg_control ltg;
ltg ltg_table[MAX_LINES];
int ltg_len;

//�߰� �κ�(pass2���� ���)
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


//�߰��κ�(output������ ���� �� ���)
//���ġ�� ���� ������ ����
typedef struct relocate {
	char name[20];//�ǿ�����
	char opra[20];//��ɾ�
	int adr;//�ּ�
	char mark[2];
}relocate;

relocate t1[5];//�ִ� ���̸� 5�� ����(COPY���α׷� ��� �뵵)
int t1_line=0;

relocate t2[5];//�ִ� ���̸� 5�� ����(RDREC���α׷� ��� �뵵)
int t2_line=0;

relocate t3[5];//�ִ� ���̸� 5�� ����(WDREC���α׷� ��� �뵵)
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

//�߰� �Լ�
int pseudo_check(char *str);
int init_pseudo_file(char *input_file);
void put_char_four(int num, char *str);
int search_symbol(int section,char *str);
char *filter_str(char *str);
int search_reference(int section, char *str);
void put_str_six(int num, char *str);

//������Ʈ ��� �Լ�
void make_symtab_output(char *file_name);
static int assem_pass2(void);
void make_objectcode_output(char *file_name);