# Utility classes used to generate resources for the Crypto service

Resource file constants.json stores JSON version of the encrypted ***constants*** file in the eric-eo-evnfm-server module.
To generate new content for the that constants file edit ***constants.json*** and run 
***CreateEncryptedConstantsFile.java*** class.

Some rules for editing values in ***constants.json*** file:

1. latestPasswordsObscurityVersion should be incremented according to its hexadecimal value: 
    - 256 = 0x0100
    - 257 = 0x0101
    - 258 = 0x0102 
      
Therefore, the next version should be 0x0103 (259).

2. passwordLength, cipherKeyIdLength and ivLength are measured in bytes, whereas cipherKeyLength is measured in bits.