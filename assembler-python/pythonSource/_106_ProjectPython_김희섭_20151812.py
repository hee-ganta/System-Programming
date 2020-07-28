#기계어 목록에 있는 정보를 저장해줄 클래스
class Instruction() :

    '''
    생성자 부분
    parsing함수를 호출한다
    입력  line : instruction 명세파일로부터 한줄씩 가져온 문자열
    '''
    def __init__(self,line):
        self.name = "" #명령어의 이름을 저장
        self.format = "" #명령어의 형식을 저장
        self.opcode = ""#명령어의 기계어 코드를 저장
        self.ops = ""#피연산자의 갯수를 저장
        self.parsing(line)

    '''
    문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
    입력 line : instruction 명세파일로부터 한줄씩 가져온 문자열
    '''
    def parsing(self,line):
        lines = line.split('\t')
        self.name = lines[0]
        if(lines[1] is "2"):
            self.format = 2
        elif(lines[1] is "3/4"):#편의상 3을 저장
            self.format = 3
        self.opcode = lines[2]
        self.ops = int(lines[3])
    

#기계어 목록 파일을 읽어와 해당 정보를 기억하는 클래스
class InstTable():

    '''
    생성자 부분
    명령어 파일을 읽어들임
    param instFile : instruction에 대한 명세가 저장된 파일 이름
    '''
    def __init__(self,instFile):
        self.instName = []#명령어 정보들을 저장할 리스트
        self.instOb = []
        self.file = ""
        self.openFile(instFile)

    '''
    입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장
    param instFile : instruction에 대한 명세가 저장된 파일 이름
    '''
    def openFile(self,instFile):
        self.file = open(instFile,'r')
        while True:
            line = self.file.readline()
            if not line :
                break
            #inst인스턴스 생성 후 정보를 instMap에 저장
            temp = Instruction(line)
            self.instName.append(temp.name)
            self.instOb.append(temp)
        self.file.close()
            

    '''
    찾고자 하는 명령어의 객체를 반환
    param name : 찾고자 하는 명령어
    retrun 명령어 객체
    '''
    def search(self,name):
        count = 0
        for i in self.instName:
            if i != name:
                count += 1
            else:
                break
        return self.instOb[count]
               


    
#각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다.
#의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
class Token():
   
    '''
    생성자 부분. 파싱 작업을 수행한다
    param line 문장단위로 저장된 프로그램 코드
    '''
    def __init__ (self,line):
        self.location = 0#위치를 저장
        self.label = ""#라벨을 저장
        self.operator = ""#연산자를 저장
        self.operand = ["","",""]#피연산자를 저장
        self.comment = ""#주석을 저장
        self.nixbpe = 0 #nixbpe비트에 대한 연산을 저장

        self.objectCode = ""#기계어코드를 저장
        self.byteSize = 0 #기계어 코드가 차지하는 바이트수를 저장

        self.locGap = 0 #현재 토큰의 pc상태와 목표주소간의 차이를 저장(오브젝트 코드 생성시 사용)
        self.parsing(line)
    
    def setLocation(self,location):
        self.location = location

    '''
    line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
    param line 문장단위로 저장된 프로그램 코드.
    '''
    def parsing(self,line):
        lines = line.split("\t")
        size = len(lines)
        if size == 1:
            lines[0] = lines[0][:len(lines[0])-1]
        if size == 2:
            lines[1] = lines[1][:len(lines[1])-1]
        if size == 3:
            lines[2] = lines[2][:len(lines[2])-1]
        if size == 4:
            lines[3] = lines[3][:len(lines[3])-1]
        if size >= 1:#라벨을 저장
            if lines[0] != "\t":
                self.label = lines[0]
        if size >= 2:#연산자를 저장
            if lines[1] != "\t":
                self.operator = lines[1]
        if size >= 3:#피연산자를 저장
            if lines[2] != "\t":
                tempLine = str(lines[2])
                buffer = ""
                num = 0
                #분리 작업
                for i in tempLine:
                    #','에 대하여 작업
                    if i is ',':
                        self.operand[num] = buffer
                        num +=1
                        buffer = ""
                    #산술 연산자에 대하여 작업
                    elif (i is '+') or (i is '-') or (i is '*')  or (i is '/')  or (i is '%'):                        
                        buffer += i#연산자도 같이 저장을 해 줌
                        self.operand[num] = buffer
                        num += 1
                        buffer =""
                    else:
                        buffer += i
                self.operand[num] = buffer                
        if size >= 4:#주석을 저장
            if lines[3] != "\t":
                self.comment = lines[3]

           
    '''
    n,i,x,b,p,e flag를  설정한다
    사용 예 : setFlag(nFlag, 1)
    또는     setFlag(TokenTable.nFlag, 1)
    param flag : 원하는 비트 위치
        value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
    '''
    def setFlag(self,flag,value):
        if value is 1:#비트가 1이면 해당 값을 더함
            self.nixbpe += flag
        else:#비트가 0이면 해당 값을 더하지 않음
            self.nixbpe += 0

    '''
    원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다
    사용 예 : getFlag(nFlag)
    또는     getFlag(nFlag|iFlag)
    param  flags : 값을 확인하고자 하는 비트 위치
    return 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임
    '''
    def getFlag(self,flag):
        return self.nixbpe & flag

#사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다.
#pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.
#section 마다 인스턴스가 하나씩 할당된다.
class TokenTable():
    #flag값을 설정
    nFlag = 32
    iFlag = 16
    xFlag = 8
    bFlag = 4
    pFlag = 2
    eFlag = 1

    #레지스터 번호를 저장
    X = '1'
    A = '0'
    S = '4'
    T = '5'


    '''
    생성자 부분
    Token을 다룰 떄 필요한 테이블들을 링크시킴
    tokenList를 만듬
    param symTab : 사용할 심볼 테이블, instTabl : 사용할 명령어 테이블
    '''
    def __init__(self,symTab,instTab):
        self.locLength = 0 #프로그램 섹션의 길이를 저장
        self.symTab = symTab
        self.instTab = instTab
        self.tokenList = []
    

    '''
    일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
    param line : 분리되지 않은 일반 문자열
    '''
    def putToken(self,token):
        self.tokenList.append(token)

    '''
    tokenList 앞부분에 있는 토큰을 빼냄
    '''
    def removeToken(self):
        self.tokenList.pop()

    '''
    tokenList에서 index에 해당하는 Token을 리턴한다.
    param index 해당 index값
    return index번호에 해당하는 코드를 분석한 Token 클래스
    '''
    def getToken(self,index):
        return self.tokenList[index]

    '''
    Pass2 과정에서 사용한다.
    instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
    param index 해당 index값
    '''
    def makeObjectCode(self,index):
        data = ""#생성되는 기계어 코드를 저장할 공간
        data2 = ""#생성되는 기계어 코드를 저장할 공간(두 문자열을 합쳐야 할 경우만 사용)

        searchToken = self.tokenList[index]

        if searchToken.byteSize == 0:#기계어 코드가 존재하지 않는 경우
            searchToken.objectCode = data
        elif searchToken.byteSize == -2:#WORD명령어 처리
            if searchToken.operand[0].isdigit():
                data = self.secimaldecimalToHex(int(searchToken.operand[0]))
                size = len(data)
                for i in range(0 , 6-size):
                    data2 += "0"
                data2 += data
                searchToken.objectCode = data2
                searchToken.byteSize = 3
            else:
                #참조되어있는 값일 경우 기계어 코드는 0으로 설정
                if (self.symTab.searchRef(searchToken.operand[0][:len(searchToken.operand[0])-1]) == 1) and (self.symTab.searchRef(searchToken.operand[1]) ==1):
                    size = len(data)
                    for i in range(0,6-size):
                        data2 += "0"
                    data2 += data
                    searchToken.objectCode = data2
                    searchToken.byteSize = 3
        elif searchToken.byteSize == -3:#BYTE명령어 처리
            if (searchToken.operand[0][0] == 'X'):
                data += searchToken.operand[0][2:len(searchToken.operand[0]) -1]
                searchToken.byteSize = len(data)/2
                searchToken.objectCode = data
            elif (searchToken.operand[0][0] == 'C'):
                for i in range(2,len(searchToken.operand[0])-1):
                    c = searchToken.operand[0][i]
                    cNum = ord(c)#아스키코드를 받아옴
                    data = secimalToHex(cNum)
                    datasearchToken.objectCode = data2
                    searchToken.byteSize = len(data)/2
        elif searchToken.byteSize == -4:#2형식일 경우
            searchInst = self.instTab.search(searchToken.operator)
            data += searchInst.opcode
            for i in range(len(data),2):
                data2 += "0"
            data2 += data
            #레지스터 부분 고려
            if searchToken.operand[0] == "":
                data2 += "0"
            elif searchToken.operand[0] == 'X':
                data2 += TokenTable.X
            elif searchToken.operand[0] == 'A':
                data2 += TokenTable.A
            elif searchToken.operand[0] == 'S':
                data2 += TokenTable.S
            elif searchToken.operand[0] == 'T':
                data2 += TokenTable.T

            if searchToken.operand[1] == "":
                data2 += "0"
            elif searchToken.operand[1] == 'X':
                data2 += TokenTable.X
            elif searchToken.operand[1] == 'A':
                data2 += TokenTable.A
            elif searchToken.operand[1] == 'S':
                data2 += TokenTable.S
            elif searchToken.operand[1] == 'T':
                data2 += TokenTable.T
            searchToken.objectCode = data2
            searchToken.byteSize = 2
        elif searchToken.byteSize == -5:#3형식일 경우
            searchInst = self.instTab.search(searchToken.operator)
            instOp = self.hexToDecimal(searchInst.opcode)
            gap = 0
            charTemp = 0
            #명령어 코드의 생성
            if searchToken.getFlag(TokenTable.nFlag) != 0:
                instOp += 2
            if searchToken.getFlag(TokenTable.iFlag) != 0:
                instOp+=1
            data = self.decimalToHex(instOp)
            
            for i  in range(len(data),2):
                data2 += "0"

            data2 += data
            if searchToken.getFlag(TokenTable.xFlag) != 0:
                charTemp += 8
            if searchToken.getFlag(TokenTable.bFlag) != 0:
                charTemp += 4
            if searchToken.getFlag(TokenTable.pFlag) != 0:
                charTemp += 2
            if searchToken.getFlag(TokenTable.eFlag) != 0:
                charTemp += 1
            data = self.decimalToHex(charTemp)
            data2 += data
            gap = searchToken.locGap
            if gap >= 0:
                data = self.decimalToHex(gap)
                for i in range(len(data),3):
                    data2 += "0"
                data2 += data
            else:#음수는 따로 처리
                num = (16*16*15) + (16 * 15) + 16
                num = num + gap
                data = self.decimalToHex(num)
                for i in range(len(data),3):
                    data2 += "0"
                data2 += data
            searchToken.objectCode = data2
            searchToken.byteSize = 3
        elif searchToken.byteSize == -6:
            operandNum = 0
            searchInst = self.instTab.search(searchToken.operator)
            instOp = self.hexToDecimal(searchInst.opcode)
            #명령어 코드 생성
            if searchToken.getFlag(TokenTable.nFlag) != 0:
                instOp +=2
            if searchToken.getFlag(TokenTable.iFlag) != 0:
                instOp += 1
            data = self.decimalToHex(instOp)
            for i in range(len(data),2):
                data2 += "0"
            data2 += data
            data2 += "0"
            operandNum = int(searchToken.operand[0][1:])
            data = self.decimalToHex(operandNum)
            for i in range(len(data),3):
                data2 += "0"
            data2 += data
            searchToken.objectCode = data2
            searchToken.byteSize = 3
        elif searchToken.byteSize == -7:
            searchInst = self.instTab.search(searchToken.operator)
            instOp= self.hexToDecimal(searchInst.opcode)
            #명령어 코드 생성
            if searchToken.getFlag(TokenTable.nFlag) != 0:
                instOp += 2
            if searchToken.getFlag(TokenTable.iFlag) != 0:
                instOp += 1
            data = self.decimalToHex(instOp)
            for i in range(len(data),2):
                data2 += "0"
            data2 += data
            for i in range(0,4):
                data2 += "0"
            searchToken.objectCode = data2
            searchToken.byteSize = 3
        elif searchToken.byteSize == -8:
            charTemp = 0
            searchInst = self.instTab.search(searchToken.operator[1:])
            instOp = self.hexToDecimal(searchInst.opcode)
            gap = 0
            if searchToken.getFlag(TokenTable.nFlag) != 0 :
                instOp += 2
            if searchToken.getFlag(TokenTable.iFlag) != 0:
                instOp += 1
            data = self.decimalToHex(instOp)
            for i in range(len(data),2):
                data2 += "0"
            data2 += data
            if searchToken.getFlag(TokenTable.xFlag) != 0:
                charTemp += 8
            if searchToken.getFlag(TokenTable.bFlag) != 0:
                charTemp += 4
            if searchToken.getFlag(TokenTable.pFlag) != 0:
                charTemp += 2
            if searchToken.getFlag(TokenTable.eFlag) != 0:
                charTemp += 1
            data = self.decimalToHex(charTemp)
            data2 += data

            gap = searchToken.locGap
            if gap >= 0:
                data = self.decimalToHex(gap)
                for i in range(len(data),5):
                    data2 += "0"
                data2 += data
            else:
                num = (16*16*15) + (16*15) + 16
                num = num + gap
                data = self.decimalToHex(num)
                for i in range(len(data),5):
                    data2 += "0"
                data2 += data
            searchToken.objectCode = data2
            searchToken.byteSize =4
        elif searchToken.byteSize == -9:
            if searchToken.operator[1] == 'C':
                temp = searchToken.operator[3:len(searchToken.operator)-1]
                for i in temp:
                    data = self.decimalToHex(ord(i))
                    data2 += data
                searchToken.objectCode = data2
                searchToken.byteSize = len(data2)/2
            elif searchToken.operator[1] == 'X':
                data = searchToken.operator[3:len(searchToken.operator)-1]
                searchToken.objectCode = data
                searchToken.byteSize = len(data)/2
 
    '''
    해당 주소값을 16진수 형태로 출력
    param addr : 16진수 형태로 바꿀 10진수 주소값
    return 16진수 형태의 문자열
    '''
    def decimalToHex(self,addr):
        hexList = ['0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F']
        res = ""
        reres = ""
        temp = 0
        while addr != 0:
            temp = addr % 16
            res += hexList[temp]
            addr = addr >> 4
        if res == "":#만약에 아무 문자도 들어가 있지 않으면
            reres = "0"
        else:
            for i in range(0,len(res)):
                reres += res[len(res)-1-i]
        return reres
    '''
    16진수 문자열을 10진수 정수로 바꾸어 출력
    param hex : 16진수 문자열
    return 10진수 정수
    '''
    def hexToDecimal(self,hex):
        res = 0
        size = len(hex)
        for i in hex:
            if i.isdigit():
                res += int(i) * (16 ** (size-1))
                size -= 1
            else:
                res += (ord(i) - 55) * (16 ** (size-1))
                size -= 1
        return res

    '''
    index번호에 해당하는 object code를 리턴한다.
    param index 해당 index값
    return object code
    '''
    def getObjectCode(self,index):
        return self.tokenList.get(index).objectCode

class SymbolTable():

    def __init__(self):
        self.symbolList = {}
        self.literalList =[]
        self.defList = []
        self.refList=[]


    '''
    새로운 Symbol을 table에 추가한다.
    param symbol : 새로 추가되는 symbol의 label
    param location : 해당 symbol이 가지는 주소값
    주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다.
    매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
    '''
    def putSymbol(self, symbol, location):
        self.symbolList[symbol] = location 
    
    '''
    리터럴에 대한 정보를 저장해준다
    param literal : 피연산자로 나온 리터럴을 저장
    '''
    def putLiteral(self,literal):
        check = 0
        for i in self.literalList:
            if i == literal:
                check = 1
        if check == 0:
            self.literalList.append(literal)
        


    '''
    정의 심볼에 대한 정보를 저장해준다
    param defSymbol : 피연산자로 나온 심볼을 저장
    '''
    def putDef(self,defSymbol):
        self.defList.append(defSymbol)

    '''
    참조 심볼에 대한 정보를 저장해준다
    refSymbol : 피연산자로 나온 리터럴을 저장
    '''
    def putRef(self,refSymbol):
        self.refList.append(refSymbol)

    '''
    참조 심볼에 해당 심볼이 있는지 검사
    param symbol: 찾고자 하는 주소값의
    return 해당 심볼이 있으면 1, 없으면 0을 반환 
    '''
    def searchRef(self,symbol):
        check = 0
        for i in self.refList:
            if i == symbol:
                check = 1
                break
        return check
            


    '''
    기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
    param symbol : 변경을 원하는 symbol의 label
    param newLocation : 새로 바꾸고자 하는 주소값
    '''
    def modifySymbol(slef,symbol, newLocation):
        #심볼이 있는 경우에만 수정을 수행함 
        if self.symbolList.get(symbol) is not None:
            self.symbolList[symbol] = location

    '''
    인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다. 
    param symbol : 검색을 원하는 symbol의 label
    return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
    '''
    def search(self,symbol):
        return self.symbolList[symbol]


#어셈블러 기능을 하는 클래스
class Assembler():
    lineList =[]#읽어들인 input파일의 내용을 한 줄 씩 저장하는 공간
    symtabList = []#프로그램의 section별로 symbol table을 저장하는 공간 
    TokenList = []#프로그램의 section별로 token table을 저장하는 공간

    codeList = []#Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간

    section = 0#프로그램의 section이 몇개인지 저장을 해줌

    def __init__(self,instFile):
        self.instTable = InstTable(instFile)#instruction 명세를 저장

    '''
    inputFile을 읽어들여서 lineList에 저장
    param inputFile : input 파일 이름.
    '''
    def loadInputFile(self,inputFile):
        idx = 0;
        self.file = open(inputFile,'r')
        while True:
            line = self.file.readline()
            if not line:
                break
            self.lineList.append(line)
        self.file.close()

    '''
    pass1 과정을 수행한다.
    1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
    2) label을 symbolTable에 정리
    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
    '''   
    def pass1(self):
        locctr = 0 #주소값을 담을 변수
        tokenSearch = 0
        searchTokenTable = 0
        #첫번째꺼는 생성
        self.symtabList.append(SymbolTable())
        self.TokenList.append(TokenTable(self.symtabList[self.section],self.instTable))
        #읽어온 라인마다 수행
        for i in range(0,len(self.lineList)):
            #조사하고자 하는 토큰과 토큰 테이블의 설정
             tokenSearch = Token(self.lineList[i])
             searchTokenTable = self.TokenList[self.section]
             if tokenSearch.label == ".":
                 continue
             self.TokenList[self.section].putToken(tokenSearch)#토큰을 추가해줌
             tokenIndex = len(self.TokenList[self.section].tokenList)
             if tokenSearch.operator == "START":#START명령어일 경우
                 #섹션의 생성
                 self.symtabList[self.section].putSymbol(tokenSearch.label,int(tokenSearch.operand[0]))
                 tokenSearch.location = int(tokenSearch.operand[0])
             elif tokenSearch.operator == "END":#END명령어일 경우
                 lSize = len(self.symtabList[self.section].literalList)
                 self.symtabList[self.section].putSymbol(tokenSearch.label,-1)
                 tokenSearch.location = -1
                 for j in range(0,lSize):
                     tempLine = self.symtabList[self.section].literalList[j] + "\t" + self.symtabList[self.section].literalList[j] + "\t\t"
                     tokenSearch = Token(tempLine)
                     searchTokenTable = self.TokenList[self.section]
                     self.TokenList[self.section].putToken(tokenSearch)#토큰을 추가해줌
                     tokenIndex = len(self.TokenList[self.section].tokenList)
                     self.symtabList[self.section].putSymbol(self.symtabList[self.section].literalList[j],locctr)#심볼테이블에 저장
                     tokenSearch.location = locctr
                     #loctor조정부분
                     if self.symtabList[self.section].literalList[j][1] == 'X':
                         num = len(self.symtabList[self.section].literalList[j])-4
                         locctr += int(num/2)
                         searchTokenTable.locLength += int(num/2)
                     elif symtabList[self.section].literalList[j][1] == 'C':
                         num = len(self.symTabList[self.section].literalList[j])-4
                         locctr += num
                         searchTokenTable.locLength += num
                 self.symtabList[self.section].literalList.clear()#티터럴 리스트 초기화 작업
             elif  tokenSearch.operator == "EXTDEF":#EXTDEF명령어일 경우
                 self.symtabList[self.section].putSymbol(tokenSearch.label,-1)#주소값이 없으므오 -1저장
                 tokenSearch.setLocation(-1)
                 for j in range(0,len(tokenSearch.operand)):
                     self.symtabList[self.section].putDef(tokenSearch.operand[j])
             elif tokenSearch.operator == "EXTREF":#EXTREF명령어일 경우
                 self.symtabList[self.section].putSymbol(tokenSearch.label,-1)
                 tokenSearch.location = -1
                 for j in range(0,len(tokenSearch.operand)):
                     self.symtabList[self.section].putRef(tokenSearch.operand[j])
             elif tokenSearch.operator == "RESW":#RESW명령어일 경우
                 self.symtabList[self.section].putSymbol(tokenSearch.label,locctr)
                 tokenSearch.location = locctr
                 locctr += 3 * int(tokenSearch.operand[0])#주소값 증가
                 searchTokenTable.locLength += 3 * int(tokenSearch.operand[0])
             elif tokenSearch.operator == "RESB":#RESB명령어일 경우
                 self.symtabList[self.section].putSymbol(tokenSearch.label,locctr)
                 tokenSearch.location = locctr
                 locctr += 1 * int(tokenSearch.operand[0])
                 searchTokenTable.locLength += 1 * int(tokenSearch.operand[0])
             elif tokenSearch.operator  == "BYTE":#BYTE명령어일 경우
                self.symtabList[self.section].putSymbol(tokenSearch.label,locctr)#해당 주소값 저장
                tokenSearch.location =locctr
                if tokenSearch.operand[0][0] == 'X':
                    num = len(tokenSearch.operand[0])-3
                    locctr += int(num/2)#주소값재설정
                    searchTokenTable.locLength += int(num/2)
                elif tokenSearch.operand[0][0] == 'C':
                    num = len(tokenSearch.operand[0]) - 3
                    locctr += num
                    searchTokenTable.locLength += num
             elif tokenSearch.operator == "WORD":#WORD명령어일 경우
                 self.symtabList[self.section].putSymbol(tokenSearch.label,locctr)#해당 주소값 저장
                 tokenSearch.location = locctr
                 locctr += 3
                 searchTokenTable.locLength += 3
             elif tokenSearch.operator == "LTORG":#LTORG명령어일 경우
                 lSize = len(self.symtabList[self.section].literalList)
                 self.symtabList[self.section].putSymbol(tokenSearch.label,-1)
                 tokenSearch.location= -1
                 for j in range(0,lSize):
                     tempLine = self.symtabList[self.section].literalList[j]+"\t" + self.symtabList[self.section].literalList[j] + "\t\t"
                     tokenSearch = Token(tempLine)
                     searchTokenTable = self.TokenList[self.section]
                     self.TokenList[self.section].putToken(tokenSearch)#토큰을 추가해줌
                     tokenIndex = len(self.TokenList[self.section].tokenList)
                     self.symtabList[self.section].putSymbol(self.symtabList[self.section].literalList[j],locctr)
                     tokenSearch.setLocation(locctr)
                     if self.symtabList[self.section].literalList[j][1] =='X':#16진수일떄의 처리
                         num = len(self.symtabList[self.section].literalList[j]) - 4
                         locctr += int(num/2)
                         searchTokenTable.locLength += int(num/2)
                     elif self.symtabList[self.section].literalList[j][1] == 'C':#문자열일떄의 처리
                         num = len(self.symtabList[self.section].literalList[j]) -4
                         locctr += num
                         searchTokenTable.locLength += num
                 self.symtabList[self.section].literalList.clear()
             elif tokenSearch.operator == "CSECT":#CSECT명령어일 경우
                 locctr = 0
                 self.TokenList[self.section].removeToken()#토큰 리스트에서 하나를 뺴줌
                 self.section = self.section + 1#섹션의 수를 하나 늘려줌
                 self.symtabList.append(SymbolTable())
                 self.TokenList.append(TokenTable(self.symtabList[self.section],self.instTable))
                 tokenSearch = Token(self.lineList[i])
                 searchTokenTable = self.TokenList[self.section]
                 self.TokenList[self.section].putToken(tokenSearch)#토큰을 추가해줌
                 tokenIndex = len(self.TokenList[self.section].tokenList)
                 self.symtabList[self.section].putSymbol(tokenSearch.label,locctr)
                 tokenSearch.location = locctr
             elif tokenSearch.operator == "EQU":#EQU명령어일 경우
                 if tokenSearch.operand[0][0] == '*':
                     self.symtabList[self.section].putSymbol(tokenSearch.label,locctr)
                     tokenSearch.location = locctr
                 else:
                     temp1 = tokenSearch.operand[0][:len(tokenSearch.operand[0])-1]
                     op = tokenSearch.operand[0][len(tokenSearch.operand[0])-1]
                     temp2 = tokenSearch.operand[1]
                     if op == '+':
                         self.symtabList[self.section].putSymbol(tokenSearch.label,self.symtabList[self.section].search(temp1) + self.symtabList[self.section].search(temp2))
                         tokenSearch.location = self.symtabList[self.section].search(temp1) + self.symtabList[self.section].search(temp2)
                     elif op == '-':
                         self.symtabList[self.section].putSymbol(tokenSearch.label,self.symtabList[self.section].search(temp1) - self.symtabList[self.section].search(temp2))
                         tokenSearch.location = self.symtabList[self.section].search(temp1) - self.symtabList[self.section].search(temp2)
                     elif op == '*':
                         self.symtabList[self.section].putSymbol(tokenSearch.label,self.symtabList[self.section].search(temp1) * self.symtabList[self.section].search(temp2))
                         tokenSearch.location = self.symtabList[self.section].search(temp1) * self.symtabList[self.section].search(temp2)
                     elif op == '/':
                         self.symtabList[self.section].putSymbol(tokenSearch.label,self.symtabList[self.section].search(temp1) / self.symtabList[self.section].search(temp2))
                         tokenSearch.location = self.symtabList[self.section].search(temp1) / self.symtabList[self.section].search(temp2)
             else:#어셈블러 자체 명령어가 아닌 경우
                 if tokenSearch.operator[0] == '+':
                     judge = tokenSearch.operator[1:]
                 else:
                     judge = tokenSearch.operator
                 
                 if self.instTable.search(judge).format == 2:#2형식일 경우
                     self.symtabList[self.section].putSymbol(tokenSearch.label,locctr)
                     tokenSearch.location = locctr
                     locctr += 2
                     searchTokenTable.locLength += 2
                     if tokenSearch.operand[0] != "":
                         if tokenSearch.operand[0][0] == '=':
                             self.symtabList[self.section].putLiteral(tokenSearch.operand[0])
                 else:
                     if tokenSearch.operator[0] != '+':#3형식일 경우
                         self.symtabList[self.section].putSymbol(tokenSearch.label,locctr)
                         tokenSearch.location = locctr
                         locctr += 3
                         searchTokenTable.locLength += 3
                         if tokenSearch.operand[0] != "":
                             if tokenSearch.operand[0][0] == '=':
                                 self.symtabList[self.section].putLiteral(tokenSearch.operand[0])
                     else:#4형식일 경우
                         self.symtabList[self.section].putSymbol(tokenSearch.label,locctr)
                         tokenSearch.location = locctr
                         locctr += 4
                         searchTokenTable.locLength += 4
                         if tokenSearch.operand[0] != "":
                             if tokenSearch.operand[0][0] == '=':
                                 self.symtabList[self.section].putLiteral(tokenSearch.operand[0])
                         
    '''
    10진수 정수를 16진수 문자열로 바꿔주는 함수
    param addr 16진수 정수
    '''
    def decimalToHex(self,addr):
        hexList = ['0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F']
        res = "" 
        reres = ""
        temp = 0
        while addr != 0:
            temp = addr % 16
            res += hexList[temp]
            addr = addr >> 4
        if res == "":#만약에 아무 문자도 들어가 있지 않으면
            reres = "0"
        else:
            for i in range(0,len(res)):
                reres += res[len(res)-1-i]
        return reres
    '''
    심볼들에 대한 주소값을 파일에 저장
    param fileNmae 파일의 이름을 저장
    '''
    def printSymbolTable(self,fileName):
        file = open(fileName,'w')
        for i in range(0,self.section+1):
            for j , k in self.symtabList[i].symbolList.items():
                if (j != "") and  (j[0] != '='):
                    file.write(j + "\t\t")
                    file.write(self.decimalToHex(int(k)))
                    file.write("\n")
            file.write("\n")  
    '''
    pass2과정을 수행한다
    1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
    '''
    def pass2(self):
        for i in range(0,self.section+1):
            size = len(self.TokenList[i].tokenList)
            for j in range(0,size):#섹션마다 토큰을 검사
                searchTokenTable = self.TokenList[i]
                searchToken = self.TokenList[i].tokenList[j]
                if searchToken.operator == "START":#START명령어일 경우
                    searchToken.byteSize = 0
                    searchToken.setFlag(TokenTable.nFlag,0)
                    searchToken.setFlag(TokenTable.iFlag,0)
                    searchToken.setFlag(TokenTable.xFlag,0)
                    searchToken.setFlag(TokenTable.bFlag,0)
                    searchToken.setFlag(TokenTable.pFlag,0)
                    searchToken.setFlag(TokenTable.eFlag,0)
                    searchTokenTable.makeObjectCode(j)
                    continue
                elif searchToken.operator == "EXTDEF":#EXTDEF명령어일 경우
                    searchToken.byteSize = 0
                    searchToken.setFlag(TokenTable.nFlag,0)
                    searchToken.setFlag(TokenTable.iFlag,0)
                    searchToken.setFlag(TokenTable.xFlag,0)
                    searchToken.setFlag(TokenTable.bFlag,0)
                    searchToken.setFlag(TokenTable.pFlag,0)
                    searchToken.setFlag(TokenTable.eFlag,0)
                    searchTokenTable.makeObjectCode(j)
                    continue
                elif searchToken.operator == "EXTREF":#EXTREF명령어일 경우
                    searchToken.byteSize = 0
                    searchToken.setFlag(TokenTable.nFlag,0)
                    searchToken.setFlag(TokenTable.iFlag,0)
                    searchToken.setFlag(TokenTable.xFlag,0)
                    searchToken.setFlag(TokenTable.bFlag,0)
                    searchToken.setFlag(TokenTable.pFlag,0)
                    searchToken.setFlag(TokenTable.eFlag,0)
                    searchTokenTable.makeObjectCode(j)
                    continue
                elif searchToken.operator == "EQU":#EQU명령어일 경우
                    searchToken.byteSize = 0
                    searchToken.setFlag(TokenTable.nFlag,0)
                    searchToken.setFlag(TokenTable.iFlag,0)
                    searchToken.setFlag(TokenTable.xFlag,0)
                    searchToken.setFlag(TokenTable.bFlag,0)
                    searchToken.setFlag(TokenTable.pFlag,0)
                    searchToken.setFlag(TokenTable.eFlag,0)
                    searchTokenTable.makeObjectCode(j)
                    continue
                elif searchToken.operator == "CSECT":#CSECT명령어일 경우
                    searchToken.byteSize = 0
                    searchToken.setFlag(TokenTable.nFlag,0)
                    searchToken.setFlag(TokenTable.iFlag,0)
                    searchToken.setFlag(TokenTable.xFlag,0)
                    searchToken.setFlag(TokenTable.bFlag,0)
                    searchToken.setFlag(TokenTable.pFlag,0)
                    searchToken.setFlag(TokenTable.eFlag,0)
                    searchTokenTable.makeObjectCode(j)
                    continue
                elif searchToken.operator == "LTORG":#LTORG명령어일 경우
                    searchToken.byteSize = 0
                    searchToken.setFlag(TokenTable.nFlag,0)
                    searchToken.setFlag(TokenTable.iFlag,0)
                    searchToken.setFlag(TokenTable.xFlag,0)
                    searchToken.setFlag(TokenTable.bFlag,0)
                    searchToken.setFlag(TokenTable.pFlag,0)
                    searchToken.setFlag(TokenTable.eFlag,0)
                    searchTokenTable.makeObjectCode(j)
                    continue
                elif searchToken.operator == "END":#END명령어일 경우
                    searchToken.byteSize = 0
                    searchToken.setFlag(TokenTable.nFlag,0)
                    searchToken.setFlag(TokenTable.iFlag,0)
                    searchToken.setFlag(TokenTable.xFlag,0)
                    searchToken.setFlag(TokenTable.bFlag,0)
                    searchToken.setFlag(TokenTable.pFlag,0)
                    searchToken.setFlag(TokenTable.eFlag,0)
                    searchTokenTable.makeObjectCode(j)
                    continue
                elif  searchToken.operator == "RESW":#RESW명령어일 경우
                    searchToken.byteSize = 0
                    searchToken.setFlag(TokenTable.nFlag,0)
                    searchToken.setFlag(TokenTable.iFlag,0)
                    searchToken.setFlag(TokenTable.xFlag,0)
                    searchToken.setFlag(TokenTable.bFlag,0)
                    searchToken.setFlag(TokenTable.pFlag,0)
                    searchToken.setFlag(TokenTable.eFlag,0)
                    searchTokenTable.makeObjectCode(j)
                    continue
                elif searchToken.operator == "RESB":#RESB명령어일 경우
                    searchToken.byteSize = 0
                    searchToken.setFlag(TokenTable.nFlag,0)
                    searchToken.setFlag(TokenTable.iFlag,0)
                    searchToken.setFlag(TokenTable.xFlag,0)
                    searchToken.setFlag(TokenTable.bFlag,0)
                    searchToken.setFlag(TokenTable.pFlag,0)
                    searchToken.setFlag(TokenTable.eFlag,0)
                    searchTokenTable.makeObjectCode(j)
                    continue
                elif searchToken.operator == "WORD":#WORD명령어일 경우
                    searchToken.byteSize = -2
                    searchToken.setFlag(TokenTable.nFlag,0)
                    searchToken.setFlag(TokenTable.iFlag,0)
                    searchToken.setFlag(TokenTable.xFlag,0)
                    searchToken.setFlag(TokenTable.bFlag,0)
                    searchToken.setFlag(TokenTable.pFlag,0)
                    searchToken.setFlag(TokenTable.eFlag,0)
                    searchTokenTable.makeObjectCode(j)
                    continue
                elif searchToken.operator == "BYTE":#BYTE명령어일 경우
                    searchToken.byteSize = -3
                    searchToken.setFlag(TokenTable.nFlag,0)
                    searchToken.setFlag(TokenTable.iFlag,0)
                    searchToken.setFlag(TokenTable.xFlag,0)
                    searchToken.setFlag(TokenTable.bFlag,0)
                    searchToken.setFlag(TokenTable.pFlag,0)
                    searchToken.setFlag(TokenTable.eFlag,0)
                    searchTokenTable.makeObjectCode(j)
                    continue
                else:
                    #명령어 분석을 위한 작업
                    if searchToken.operator[0] != '+':
                        checkOperator = searchToken.operator
                    else:
                        checkOperator = searchToken.operator[1:]
                     
                    if checkOperator[0] != '=':#리터럴이 아닐 때에만 명령어 판별
                        checkInst = self.instTable.search(checkOperator)
                    else:
                        checkInst = None

                    if checkInst == None:#리터럴일떄의 작업
                        searchToken.byteSize = -9
                        searchToken.setFlag(TokenTable.nFlag,0)
                        searchToken.setFlag(TokenTable.iFlag,0)
                        searchToken.setFlag(TokenTable.xFlag,0)
                        searchToken.setFlag(TokenTable.bFlag,0)
                        searchToken.setFlag(TokenTable.pFlag,0)
                        searchToken.setFlag(TokenTable.eFlag,0)
                        searchTokenTable.makeObjectCode(j)
                        continue
                    elif checkInst.format == 2:#2형식인 졍우
                        searchToken.byteSize = -4
                        searchToken.setFlag(TokenTable.nFlag,0)
                        searchToken.setFlag(TokenTable.iFlag,0)
                        searchToken.setFlag(TokenTable.xFlag,0)
                        searchToken.setFlag(TokenTable.bFlag,0)
                        searchToken.setFlag(TokenTable.pFlag,0)
                        searchToken.setFlag(TokenTable.eFlag,0)
                        searchTokenTable.makeObjectCode(j)
                        continue
                    elif searchToken.operator[0] != '+':#3형식일 경우
                        searchToken.byteSize = -5#3형식임을 나타냄
                        if len(searchToken.operand[0]) == 0:#피연산자가 없는 경우 따로 설정
                            searchToken.byteSize = -7#immediate는 따로 처리
                            searchToken.setFlag(TokenTable.nFlag,1)
                            searchToken.setFlag(TokenTable.iFlag,1)
                            searchToken.setFlag(TokenTable.xFlag,0)
                            searchToken.setFlag(TokenTable.bFlag,0)
                            searchToken.setFlag(TokenTable.pFlag,0)
                            searchToken.setFlag(TokenTable.eFlag,0)
                            searchTokenTable.makeObjectCode(j)
                            continue
                        if searchToken.operand[0][0] == '#':
                            searchToken.byteSize = -6#immediate는 따로 처리
                            searchToken.setFlag(TokenTable.nFlag,0)
                            searchToken.setFlag(TokenTable.iFlag,1)
                            searchToken.setFlag(TokenTable.xFlag,0)
                            searchToken.setFlag(TokenTable.bFlag,0)
                            searchToken.setFlag(TokenTable.pFlag,0)
                            searchToken.setFlag(TokenTable.eFlag,0)
                            searchTokenTable.makeObjectCode(j)
                            continue
                        elif searchToken.operand[0][0] == '@':#indirect
                            searchToken.setFlag(TokenTable.nFlag,1)
                            searchToken.setFlag(TokenTable.iFlag,0)
                        else:
                            searchToken.setFlag(TokenTable.nFlag,1)#simple
                            searchToken.setFlag(TokenTable.iFlag,1)
                        
                        #x비트 설정
                        if searchToken.operand[0] == 'X':
                            searchToken.setFlag(TokenTable.xFlag,1)
                        else:
                            searchToken.setFlag(TokenTable.xFlag,0)

                        #b,p설정단계
                        pc = searchToken.location + 3
                        searchSymbol = searchTokenTable.symTab
                        if searchToken.operand[0][0] == '@':
                            checkOperand = searchToken.operand[0][1:]
                        else:
                            checkOperand = searchToken.operand[0]

                        targetAddr = searchSymbol.search(checkOperand)
                        
                        locGap = targetAddr - pc
                        searchToken.locGap = locGap

                        if (locGap >= 4096) or (locGap <= - 4096):#이런 상황일 경우 base relatice
                            searchToken.setFlag(TokenTable.bFlag,1)
                            searchToken.setFlag(TokenTable.pFlag,0)
                        else:#아닐 경우에는 pc relative
                            searchToken.setFlag(TokenTable.bFlag,0)
                            searchToken.setFlag(TokenTable.pFlag,1)
                        
                                        
                        searchToken.setFlag(TokenTable.eFlag,0)
                        searchTokenTable.makeObjectCode(j)
                        continue

                    elif searchToken.operator[0] == '+':#4형식일 경우
                        searchToken.byteSize = -8
                        searchToken.setFlag(TokenTable.nFlag,1)
                        searchToken.setFlag(TokenTable.iFlag,1)
                        if searchToken.operand[1] == 'X':
                            searchToken.setFlag(TokenTable.xFlag,1)
                        else:
                            searchToken.setFlag(TokenTable.xFlag,0)
                        searchToken.setFlag(TokenTable.bFlag,0)
                        searchToken.setFlag(TokenTable.pFlag,0)
                        searchToken.setFlag(TokenTable.eFlag,1)

                        pc = searchToken.location+3
                        searchSymbol = searchTokenTable.symTab
                        if searchSymbol.searchRef(searchToken.operand[0]) == 1:#참조 테이블에 있는 심볼이면 0으로 설정
                            locGap = 0
                            searchToken.locGap = locGap
                        else:
                            checkOperand = searchToken.operand[0]
                            targetAddr = searchSymbol.search(checkOperand)

                            locGap = targetAddr = pc#차이를 저장
                            searchToken.locGap = locGap
                        searchTokenTable.makeObjectCode(j)
                        continue
    '''
    오브젝트 프로그램을 생성하는 함수
    param : fileName : 저장되는 파일 이름
    '''
    def printObjectCode(self,fileName):
        file = open(fileName,'w')
        data = ""
        data2 = ""
        startCheck = 0#오브젝트 코드 한 라인의 시작부분인지 아닌지를 저장
        codeLength = 0#오브젝트코드 한 줄의 길이를 저장
        startAddr = 0#프로그램 섹션이 시작되는 주소에 대한 정보를 저장
        refInfo = []#Modification record작성시 필요한 토큰들의 정보를 저장
        for i in range(0,self.section+1):
            searchTokenTable = self.TokenList[i]
            searchToken = searchTokenTable.getToken(0)
            searchSymbol = searchTokenTable.symTab
            #Head record부분
            data2 += 'H'
            data2 += searchToken.label
            for j in range(len(data2),7):
                data2 += " "
            
            if searchToken.operand[0] != "":#시작 주소가 있으면 해당 정보를 저장
                startAddr = int(searchToken.operand[0])
            else:
                startAddr = 0
            data = self.decimalToHex(startAddr)
            for j in range(len(data),6):
                data2 += "0"
            data2 += data

            data = self.decimalToHex(searchTokenTable.locLength)
            for j in range(len(data),6):
                data2 += "0"
            data2 += data
            data2 += "\n"
            self.codeList.append(data2)
            data = ""#초기화작업
            data2 = ""

            #Define record, Refer record부분
            for j in range(0,len(searchTokenTable.symTab.defList)):
                if j == 0:
                    data2 += "D"
                defAddr = 0#정의 심볼에 대한 주소값을 받아오는 변수
                data = searchTokenTable.symTab.defList[j]
                data2 += data
                for k in range(len(data),6):
                    data2 += " "
                defAddr = searchTokenTable.symTab.search(data)
                data = self.decimalToHex(defAddr)
                for k in range(len(data),6):
                    data2 += "0"
                data2 += data
            if j >= 1:
                data2 += "\n"
                self.codeList.append(data2)#Define record저장
                data = ""
                data2 = ""
            for j in range(0,len(searchTokenTable.symTab.refList)):
                if j ==0:
                    data2 += "R"
                data = searchTokenTable.symTab.refList[j]
                data2 += data
                for k in range(len(data),6):
                    data2 += " "
            if j >= 1:
                data2 += "\n"
                self.codeList.append(data2)#Refer record저장
                data = ""
                data2 = ""


            #Text record부분
            for j in range(0,len(searchTokenTable.tokenList)):
                searchToken = searchTokenTable.tokenList[j]
                if (searchSymbol.searchRef(searchToken.operand[0]) == 1) and (searchToken.operator != "EXTREF"):#피연산자가 참조 심볼이라면 해당 정보를 저장
                    refInfo.append(searchToken)
                if searchToken.operand[0] != "":
                    if searchToken.operand[0][len(searchToken.operand[0])-1] == '-':
                        if searchSymbol.searchRef(searchToken.operand[0][:len(searchToken.operand[0])-1]) == 1:
                            refInfo.append(searchToken)
                    
                if searchToken.objectCode == "":#기계어가 없으면 고려하지 않음
                    if (searchToken.operator == "RESW") or (searchToken.operator == "RESB"):
                        if(data2 != ""):
                            temp1 = ""
                            temp2 =""
                            len1 = ""
                            len2 = ""
                            len1 = self.decimalToHex(int(codeLength))
                            for k in range(len(len1),2):
                                len2 += "0"
                            data2 += "\n"
                            len2 += len1
                            temp1 = data2[:7]
                            temp2 = data2[7:]
                            data2 = ""
                            data2 = temp1 + len2 + temp2#코드길이 정보를 넣어줌
                            codeLength = 0#길이 리셋
                            startCheck = 0#조건을 바꿔줌
                            self.codeList.append(data2)
                            data = ""
                            data2 = ""
                    continue
                if startCheck == 0:#텍스트 레코드의 시작 부분
                    startCheck+=1
                    data2 += "T"
                    data = self.decimalToHex(searchToken.location)
                    for k in range(len(data),6):
                        data2 += "0"
                    data2 += data#목적어 코드의 생성
                    data2 += searchToken.objectCode
                    codeLength += searchToken.byteSize#길이를 카운트
                elif startCheck == 1:
                    codeLength += searchToken.byteSize#길이를 카운트
                    if codeLength  >= 30:#Text record를 끊어줄 조건(길이가 30이하)
                        len1 =""
                        len2 = ""
                        temp1 =""
                        temp2 = ""
                        len1 = self.decimalToHex(int(codeLength-searchToken.byteSize))
                        for k in range(len(len1),2):
                            len2 += "0"
                        len2 += len1
                        data2 += "\n"
                        temp1 = data2[:7]
                        temp2 = data2[7:]
                        data2 = ""
                        data2 = temp1 + len2 + temp2
                        codeLength = 0#길이 리셋
                        #startCheck = 0#조건을 바꿔줌
                        self.codeList.append(data2)
                        data = ""
                        data2 = ""
                        data2 += "T"
                        data = self.decimalToHex(searchToken.location)
                        for k in range(len(data),6):
                            data2 += "0"
                        data2 += data#목적어 코드의 생성
                        data2 += searchToken.objectCode
                        codeLength += searchToken.byteSize#길이를 카운트
                    else:
                        data2 += searchToken.objectCode
            if data2 != "":#넣어줘야 할 정보들이 있다면 넣어줌
                len1 =""
                len2 = ""
                temp1 =""
                temp2 = ""
                len1 = self.decimalToHex(int(codeLength))
                for k in range(len(len1),2):
                    len2 += "0"
                len2 += len1
                data2 += "\n"
                temp1 = data2[:7]
                temp2 = data2[7:]
                data2 = ""
                data2 = temp1 + len2 + temp2
                codeLength = 0#길이 리셋
                startCheck = 0#조건을 바꿔줌
                self.codeList.append(data2)
                data = ""
                data2 = ""
                        
            #Modification record부분
            op= ""
            for j in range(0, len(refInfo)):
                if refInfo[j].operator != "WORD":
                    for k in range(0,3):
                        check = refInfo[j].operand[k]
                        if (check != "") and (check != "X"):
                            data2 += "M"
                            data = self.decimalToHex(refInfo[j].location+1)
                            for l in range(len(data),6):
                                data2 += "0"
                            data2 += data
                            data2 += "05"
                            if k == 0:
                                data2 += "+"
                                if(refInfo[j].operand[k][len(refInfo[j].operand[k])-1] == '-') or (refInfo[j].operand[k][len(refInfo[j].operand[k])-1] == '-'):
                                    op = refInfo[j].operand[k][len(refInfo[j].operand[k])-1]
                                    data2 += refInfo[j].operand[k][:len(refInfo[j].operand[k])-1]
                                else:
                                    data2 += refInfo[j].operand[k]
                            else:
                                if op != "":
                                    data2 += op
                                    data2 += refInfo[j].operand[k]
                                else:
                                    data2 += "+"
                                    data2 += refInfo[j].operand[k]
                            data2 += "\n"
                            self.codeList.append(data2)#modification 레코드 부분 저장
                            data = ""
                            data2 = ""
                else:
                    for k in range(0,3):
                        check = refInfo[j].operand[k]
                        if (check != "") and (check != "X"):
                            data2 += "M"
                            data = self.decimalToHex(refInfo[j].location)
                            for l in range(len(data),6):
                                data2 += "0"
                            data2 += data
                            data2 += "06"
                            if k == 0:
                                data2 += "+"
                                if(refInfo[j].operand[k][len(refInfo[j].operand[k])-1] == '-') or (refInfo[j].operand[k][len(refInfo[j].operand[k])-1] == '-'):
                                    op = refInfo[j].operand[k][len(refInfo[j].operand[k])-1]
                                    data2 += refInfo[j].operand[k][:len(refInfo[j].operand[k])-1]
                                else:
                                    data2 += refInfo[j].operand[k]
                            else:
                                if op != "":
                                    data2 += op
                                    data2 += refInfo[j].operand[k]
                                else:
                                    data2 += "+"
                                    data2 += refInfo[j].operand[k]
                            data2 += "\n"
                            self.codeList.append(data2)#modification 레코드 부분 저장
                            data = ""
                            data2 = ""
            refInfo.clear()
            #End record부분
            if searchTokenTable.getToken(0).operator == "START":
                data2 += "E"
                data = self.decimalToHex(int(searchTokenTable.getToken(0).operand[0]))
                for j in range(len(data),6):
                    data2 += "0"
                data2 += "\n\n"
                self.codeList.append(data2)
                data = ""
                data2 = ""
            else:
                data2 += "E"
                data2 += data
                data2 += "\n\n"
                self.codeList.append(data2)
                data = ""
                data2 = ""
        for i in range(0,len(self.codeList)):
            if self.codeList[i] != "\n":#공백란이 아니라면 출력을 해줌
                file.write(self.codeList[i])
        file.close()

#어셈블러를 작동시킴
assembler = Assembler("inst.txt")
assembler.loadInputFile("input.txt")

assembler.pass1()
assembler.printSymbolTable("symtab_20151812.txt")

assembler.pass2()
assembler.printObjectCode("output_20151812.txt")


