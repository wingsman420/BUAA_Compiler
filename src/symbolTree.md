# 什么地方可能出现定义：
Del
Def


# 需要进入下一层：
就是出现大括号
通过辅助函数判断是函数定义还是纯粹的语句块

# 检查错误
*常量定义 ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // b


*变量定义 VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // b


*函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // b g

*主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // g

*函数形参 FuncFParam → BType Ident ['[' ']'] // b

语句 Stmt → *LVal '=' Exp ';' // h
| [Exp] ';'
| Block
| 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
*| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // h
| 'break' ';' | 'continue' ';' // m
*| 'return' [Exp] ';' // f
*| LVal '=' 'getint''('')'';' // h
*| LVal '=' 'getchar''('')'';' // h
*| 'printf''('StringConst {','Exp}')'';' // l

*语句 ForStmt → LVal '=' Exp // h

*左值表达式 LVal → Ident ['[' Exp ']'] // c

*一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // c d e

b c d e f g h l m

剩余 l m


