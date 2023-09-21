;Subroutine for displaying a series of lines of text
;  The lines of text display a count-down, from X to 0
;
;234567890123456789012345678901234567890
Mesg     .ORIG           
         .ENT    Displ,X
Txt      .STRZ   "Value= "
X        .FILL   #6
SavR0    .BLKW   #1
SavR1    .BLKW   #1
SavR7    .BLKW   #1
;
Displ    ST      R0,SavR0       ;save reg that will be over-written
         ST      R1,SavR1
         ST      R7,SavR7
         LD      R1,X           ;r1 <- M[X]
         BRN     Done           ;if (r1 < 0) goto Done
Loop     LEA     R0,Txt
         TRAP    x22            ;Display text "Value=  "
         LD      R0,X
         TRAP    x31            ;Display value in M[X]
         ADD     R0,R0,#-1
         ST      R0,X           ;M[X] <- r0
         BRN     Done           ;if (r0 < 0) goto Done
         JMP     Loop           ;goto Loop
Done     LD      R0,SavR0       ;restore registers
         LD      R1,SavR1
         LD      R7,SavR7
         RET                  
         .END    Displ
