import vorpal from 'vorpal'
import { connect } from 'net'
import { Message } from './Message'
const chalk = require('chalk')

export const cli = vorpal()

let username
let server
let commandCounter
cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host] [port]')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function ({username: user, host = 'localhost', port = 8080}, callback) {
    username = user
    server = connect({ host: host, port: port }, () => {
      console.log(this.username + 'entry')
      console.log(this.command + 'entry')
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => { // Hard to read, maybe do something
      if (Message.fromJSON(buffer).command === null) {
        console.log('Please Enter Valid Command')
      } else if (Message.fromJSON(buffer).command === 'connect') {
        this.log(chalk.green(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command === 'disconnect') {
        this.log(chalk.green(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command === 'echo') {
        this.log(chalk.magenta(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command === 'broadcast') {
        console.log('what the flying fuck is going on')
        this.log(chalk.cyan(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command.charAt(0) === '@') {
        this.log(chalk.white(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command === 'users') {
        this.log(chalk.red(Message.fromJSON(buffer).toString()))
      }
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = input.split(' ')
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === undefined) {
      this.log(`Enter A Valid Command`)
    } else if (command === 'echo' || command === 'broadcast' || command === 'users' ||
      command.charAt(0) === '@') {
      commandCounter = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else {
      let fullMessage = command + ` ` + contents
      if (commandCounter === 'echo' || commandCounter === 'broadcast' ||
      commandCounter === 'users' || commandCounter.charAt(0) === '@') {
        let contents = fullMessage
        let command = commandCounter
        console.log(contents + 'this is commandCounter')
        console.log(command + 'this is commandCounter')
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
      } else {
        this.log(`Command <${command}> was not recognized.  Please enter usable Command`)
      }
    }
    callback()
  })
