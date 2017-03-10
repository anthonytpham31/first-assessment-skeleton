import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'
const chalk = require('chalk')

export const cli = vorpal()

let username
let server

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> <host> <port>')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    let host = args.host
    let port = args.port
    server = connect({ host: host, port: port }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => { // Hard to read, maybe do something
      if (Message.fromJSON(buffer).command === 'connect') {
        this.log(chalk.green(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command === 'disconnect') {
        this.log(chalk.green(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command === 'echo') {
        this.log(chalk.magenta(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command === 'broadcast') {
        this.log(chalk.cyan(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command === null) {
        Message.fromJSON(buffer).command = `connect`
        this.log(chalk.white(Message.fromJSON(buffer).toString()))
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
    const [ command, ...rest ] = words(input, /[^,\s]+/g)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo' || command === 'broadcast' || command === 'users' ||
      command.charAt(0) === '@') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else {
      this.log(`Command <${command}> was not recognized.  Please enter usable Command`)
    }

    callback()
  })
