package main

import (
	"bufio"
	"flag"
	"fmt"
	"os"
)

func main() {

	wrkParams, err := validateArgs()

	if err != nil {
		println(err.Error())
		printUsage()
		os.Exit(1)
	}

	fmt.Printf("Number of loops: %d, Number of processes: %d\n", wrkParams.times, wrkParams.procs)

	//waitForUserInput()

	controller, err := createAndRunController(wrkParams)

	if err != nil {
		println(err.Error())
		os.Exit(1)
	}

	fmt.Println("Started")

	controller.startMessaging()

	controller.printFinalTimes()

	os.Stdout.Sync()
}

func printUsage() {
	fmt.Println("Usage: go run ringMsg.go -m <number of loops> -n <number of processes>")
	fmt.Println("Example: go run ringMsg.go-n 5  -m 2 ")
	fmt.Println("Options:")
	fmt.Println("  -m    Number of loops (must be a positive integer)")
	fmt.Println("  -n    Number of processes (must be a positive integer)")
}

func validateArgs() (loopControl WorkParameters, err error) {

	loopControl = WorkParameters{}

	if len(os.Args) < 3 {
		err = fmt.Errorf("invalid number of arguments %d", len(os.Args))
		return
	}

	m := flag.Int("m", 0, "number of loops")
	n := flag.Int("n", 0, "number of processes")

	flag.Parse()

	if m == nil || n == nil {
		err = fmt.Errorf("incorrect flags provided")
		return
	} else if *m <= 0 || *n <= 0 {
		err = fmt.Errorf("flag values must be positive integers m %d, n %d", *m, *n)
		return
	}

	loopControl = WorkParameters{times: *m, procs: *n}

	return
}

func waitForUserInput() {
	fmt.Println("This program will wait for you to press Enter.")
	fmt.Print("Press 'Enter' to continue...")

	// Create a new reader for standard input
	reader := bufio.NewReader(os.Stdin)

	// Read until the newline character ('\n') is encountered
	_, err := reader.ReadBytes('\n')
	if err != nil {
		fmt.Println("Error reading input:", err)
		return
	}

}
