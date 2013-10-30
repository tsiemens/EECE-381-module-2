LIBRARY ieee;

USE ieee.std_logic_1164.all; 

USE ieee.std_logic_arith.all; 

USE ieee.std_logic_unsigned.all; 

ENTITY module2nios IS

   PORT (

      SW : IN STD_LOGIC_VECTOR(7 DOWNTO 0);
      KEY : IN STD_LOGIC_VECTOR(0 DOWNTO 0);
      CLOCK_50 : IN STD_LOGIC;
      LEDG : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
      DRAM_CLK, DRAM_CKE : OUT STD_LOGIC;
      DRAM_ADDR : OUT STD_LOGIC_VECTOR(11 DOWNTO 0);
      DRAM_BA_0, DRAM_BA_1 : BUFFER STD_LOGIC;
      DRAM_CS_N, DRAM_CAS_N, DRAM_RAS_N, DRAM_WE_N : OUT STD_LOGIC;
      DRAM_DQ : INOUT STD_LOGIC_VECTOR(15 DOWNTO 0);
      DRAM_UDQM, DRAM_LDQM : BUFFER STD_LOGIC;
		LCD_DATA : inout STD_LOGIC_VECTOR(7 downto 0);
		LCD_ON, LCD_BLON, LCD_EN, LCD_RS, LCD_RW : out STD_LOGIC;
		PS2_CLK, PS2_DAT : inout STD_LOGIC;
		VGA_R:out std_logic_vector(9 downto 0);
		VGA_G : out std_logic_vector(9 downto 0);
		VGA_B : out std_logic_vector(9 downto 0);
		VGA_CLK : out std_logic;
		VGA_BLANK : out std_logic;
		VGA_HS : out std_logic;
		VGA_VS : out std_logic;
		VGA_SYNC : out std_logic;
		SRAM_DQ : INOUT STD_LOGIC_VECTOR(15 downto 0);
		SRAM_ADDR : OUT STD_LOGIC_VECTOR(17 downto 0);
		SRAM_LB_N : OUT STD_LOGIC;
		SRAM_UB_N : OUT STD_LOGIC;
		SRAM_CE_N : OUT STD_LOGIC;
		SRAM_OE_N : OUT STD_LOGIC;
		SRAM_WE_N : OUT STD_LOGIC;
		SD_DAT3 : INOUT STD_LOGIC;
		SD_DAT : INOUT STD_LOGIC;
		SD_CMD : INOUT STD_LOGIC;
		SD_CLK : OUT STD_LOGIC;
		I2C_SCLK : OUT STD_LOGIC;
		I2C_SDAT : INOUT STD_LOGIC;
		AUD_XCK : OUT STD_LOGIC;
		CLOCK_27 : IN STD_LOGIC;
		AUD_ADCDAT : IN STD_LOGIC;
		AUD_ADCLRCK : IN STD_LOGIC;
		AUD_BCLK : IN STD_LOGIC;
		AUD_DACDAT : OUT STD_LOGIC;
		AUD_DACLRCK : IN STD_LOGIC
		
		);

   END module2nios;



ARCHITECTURE Structure OF module2nios IS

   COMPONENT nios_system PORT (

      clk_clk : IN STD_LOGIC;
      reset_reset_n : IN STD_LOGIC;
      sdram_clk_clk : OUT STD_LOGIC;
      leds_export : OUT STD_LOGIC_VECTOR(7 DOWNTO 0);
      switches_export : IN STD_LOGIC_VECTOR(7 DOWNTO 0);
      sdram_wire_addr : OUT STD_LOGIC_VECTOR(11 DOWNTO 0);
      sdram_wire_ba : BUFFER STD_LOGIC_VECTOR(1 DOWNTO 0);
      sdram_wire_cas_n : OUT STD_LOGIC;
      sdram_wire_cke : OUT STD_LOGIC;
      sdram_wire_cs_n : OUT STD_LOGIC;
      sdram_wire_dq : INOUT STD_LOGIC_VECTOR(15 DOWNTO 0);
      sdram_wire_dqm : BUFFER STD_LOGIC_VECTOR(1 DOWNTO 0);
      sdram_wire_ras_n : OUT STD_LOGIC;
      sdram_wire_we_n : OUT STD_LOGIC;
		lcd_data_DATA : inout STD_LOGIC_VECTOR(7 downto 0);
		lcd_data_ON : out STD_LOGIC;
		lcd_data_BLON : out STD_LOGIC;
		lcd_data_EN : out STD_LOGIC;
		lcd_data_RS : out STD_LOGIC;
		lcd_data_RW : out STD_LOGIC;
		ps2_CLK : INOUT STD_LOGIC;
		ps2_DAT : INOUT STD_LOGIC;
		vga_controller_CLK : OUT STD_LOGIC;
		vga_controller_HS : OUT STD_LOGIC;
		vga_controller_VS : OUT STD_LOGIC;
		vga_controller_BLANK : OUT STD_LOGIC;
		vga_controller_SYNC	 : OUT STD_LOGIC;
		vga_controller_R	: OUT STD_LOGIC_VECTOR(9 downto 0);
		vga_controller_G : OUT STD_LOGIC_VECTOR(9 downto 0);
		vga_controller_B : OUT STD_LOGIC_VECTOR(9 downto 0);
		sram_DQ              : inout std_logic_vector(15 downto 0) := (others => 'X'); -- DQ
		sram_ADDR            : out   std_logic_vector(17 downto 0);                    -- ADDR
		sram_LB_N            : out   std_logic;                                        -- LB_N
		sram_UB_N            : out   std_logic;                                        -- UB_N
		sram_CE_N            : out   std_logic;                                        -- CE_N
		sram_OE_N            : out   std_logic;                                        -- OE_N
		sram_WE_N            : out   std_logic;                                       -- WE_N
		sd_card_b_SD_cmd   : inout std_logic;
		sd_card_b_SD_dat   : inout std_logic;
		sd_card_b_SD_dat3  : inout std_logic;
		sd_card_o_SD_clock : out   std_logic;
		audio_and_video_SCLK 	: out std_logic;
		audio_and_video_SDAT 	: inout std_logic;
		audio_clk_clk 			: out std_logic;
		clk_secondary_clk 	: in std_logic;
		audio_ADCDAT         : in    std_logic;
		audio_ADCLRCK        : in    std_logic;
		audio_BCLK           : in    std_logic;
		audio_DACDAT         : out   std_logic;
		audio_DACLRCK        : in    std_logic
	);
 
   END COMPONENT;

   SIGNAL DQM : STD_LOGIC_VECTOR(1 DOWNTO 0);
   SIGNAL BA : STD_LOGIC_VECTOR(1 DOWNTO 0);

   BEGIN

      DRAM_BA_0 <= BA(0);
      DRAM_BA_1 <= BA(1);
      DRAM_UDQM <= DQM(1);
      DRAM_LDQM <= DQM(0);
		
      NiosII: nios_system PORT MAP (
		
			clk_clk => CLOCK_50,
			reset_reset_n => KEY(0),
			sdram_clk_clk => DRAM_CLK,
			leds_export => LEDG,
			switches_export => SW,
         sdram_wire_addr => DRAM_ADDR,
         sdram_wire_ba => BA,
         sdram_wire_cas_n => DRAM_CAS_N,
         sdram_wire_cke => DRAM_CKE,
         sdram_wire_cs_n => DRAM_CS_N,
         sdram_wire_dq => DRAM_DQ,
         sdram_wire_dqm => DQM,
         sdram_wire_ras_n => DRAM_RAS_N,
         sdram_wire_we_n => DRAM_WE_N,
			lcd_data_DATA => LCD_DATA,
			lcd_data_ON => LCD_ON,
			lcd_data_EN => LCD_EN,
			lcd_data_RS => LCD_RS,
			lcd_data_RW => LCD_RW,
			lcd_data_BLON => LCD_BLON,
			ps2_CLK => PS2_CLK,
			ps2_DAT => PS2_DAT,
			vga_controller_CLK   => VGA_CLK,   -- vga_controller.CLK
			vga_controller_HS    => VGA_HS,    --               .HS
			vga_controller_VS    => VGA_VS,    --               .VS
			vga_controller_BLANK => VGA_BLANK, --               .BLANK
			vga_controller_SYNC  => VGA_SYNC,  --               .SYNC
			vga_controller_R     => VGA_R,     --               .R
			vga_controller_G     => VGA_G,     --               .G
			vga_controller_B     => VGA_B,     --               .B
			sram_DQ => SRAM_DQ,
			sram_ADDR => SRAM_ADDR,
			sram_LB_N => SRAM_LB_N,
			sram_UB_N => SRAM_UB_N,
			sram_CE_N => SRAM_CE_N,
			sram_OE_N => SRAM_OE_N,
			sram_WE_N => SRAM_WE_N,
			sd_card_b_SD_dat3 => SD_DAT3,
			sd_card_b_SD_dat => SD_DAT,
			sd_card_b_SD_cmd => SD_CMD,
			sd_card_o_SD_clock => SD_CLK,
			audio_and_video_SDAT => I2C_SDAT,
			audio_and_video_SCLK => I2C_SCLK,
			audio_clk_clk => AUD_XCK,
			clk_secondary_clk => CLOCK_27,
			audio_ADCDAT => AUD_ADCDAT,
			audio_ADCLRCK => AUD_ADCLRCK,
			audio_BCLK => AUD_BCLK,
			audio_DACDAT => AUD_DACDAT,
			audio_DACLRCK => AUD_DACLRCK
			
		);

   END Structure;