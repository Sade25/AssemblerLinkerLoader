;234567890123456789012345678901234567890
;label___opppp___operandsandcomments...
;
Data     .ORIG      
         .EXT    X
         .ENT    V
V        .FILL   #2
         TRAP    x43
Done     TRAP    x25
         LD      R1,=#1
         .END    Done
