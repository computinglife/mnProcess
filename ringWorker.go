package main

import (
	"fmt"
	"sync"
	"time"
)

type WorkParameters struct {
	times int // number of loops
	procs int // number of processes
}

func log(format string, a ...interface{}) {
	//fmt.Printf(format, a...)
}

type MxNWorkerController struct {
	total         WorkParameters
	startedWg     *sync.WaitGroup
	doneWg        *sync.WaitGroup
	wrkrChan      []chan int
	startSetup    time.Time
	setupDuration time.Duration
	startMsg      time.Time
	msgDuration   time.Duration
}

func (w *MxNWorkerController) markSetupStart() {
	w.startSetup = time.Now()
}

func (w *MxNWorkerController) markSetupEnd() {
	w.setupDuration = time.Since(w.startSetup)
}

func (w *MxNWorkerController) markMsgStart() {
	w.startMsg = time.Now()
}

func (w *MxNWorkerController) markMsgEnd() {
	w.msgDuration = time.Since(w.startMsg)
}

func (w *MxNWorkerController) printFinalTimes() {

	fmt.Printf("Setup %d Run %d", w.setupDuration.Milliseconds(),
		w.msgDuration.Milliseconds())
}

func (w *MxNWorkerController) startWork() {

	w.markSetupStart()

	w.doneWg.Add(1)

	for i := range w.total.procs {
		w.startedWg.Add(1)
		w.wrkrChan[i] = make(chan int)
		go w.worker(i)
	}
}

func (w *MxNWorkerController) worker(id int) {

	w.startedWg.Done()

	w.performWork(id)
}

func IsInRange(n, min, max int) bool {
	return n >= min && n <= max
}

func (w *MxNWorkerController) performWork(id int) {

	times := 0

	for times < w.total.times {

		times++

		data := <-w.wrkrChan[id]

		w.validateInput(id, data)

		data = w.getDataToBeSent(data)

		w.sendData(id, times, data)

		if times == w.total.times {
			log("closing channel for %d \n", id)
			close(w.wrkrChan[id])
		}

		w.signalDoneIfLast(id, times)
	}

	log("routine %d exiting \n", id)
}

func (w *MxNWorkerController) validateInput(id int, input int) {

	//log("routine %d recieved data %d \n", id, input)

	if !IsInRange(input, 0, w.total.procs) {
		panic(fmt.Sprintf("unexpected range of data input: %d", input))
	}
}

func (w *MxNWorkerController) getDataToBeSent(data int) int {

	if data < w.total.procs-1 {
		data++
	} else {
		data = 0
	}
	return data
}

func (w *MxNWorkerController) sendData(id, times int, data int) {

	if times < w.total.times || id != w.total.procs-1 {
		//log("About to send data %d to channel %d \n", data, data)
		w.wrkrChan[data] <- data
		//log("Finished sending data %d to channel %d \n", data, data)
	}
}

func (w *MxNWorkerController) signalDoneIfLast(id int, times int) {

	if times == w.total.times && id == w.total.procs-1 {

		w.doneWg.Done()
	}

}

func createAndRunController(wrkParams WorkParameters) (*MxNWorkerController, error) {

	wrk, err := startWork(wrkParams)

	if err != nil {
		return wrk, err
	}

	wrk.startedWg.Wait()

	wrk.markSetupEnd()

	return wrk, nil
}

func startWork(loop WorkParameters) (wrk *MxNWorkerController, err error) {

	wrk = &MxNWorkerController{
		total:     loop,
		startedWg: &sync.WaitGroup{},
		doneWg:    &sync.WaitGroup{},
		wrkrChan:  make([]chan int, loop.procs)}

	wrk.startWork()

	return
}

func (w *MxNWorkerController) startMessaging() {

	w.markMsgStart()

	w.wrkrChan[0] <- 0

	w.doneWg.Wait()

	w.markMsgEnd()
}
