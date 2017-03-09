import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'
const chalk = require('chalk')

export const cli = vorpal()

let username
let server
// let host
// let port
let timestamp = new Date()
timestamp = timestamp.toLocaleString(
  'en-US', {
    hour: 'numeric',
    minute: 'numeric',
    second: 'numeric',
    hour12: true
  }
)

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username>')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    // host = args.host
    // port = args.port
    server = connect({ host: 'localhost', port: 8080 }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
      if (Message.fromJSON(buffer).command === 'echo') {
        this.log(timestamp + ` ` + username.toString ` : ` + chalk.red(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command === 'broadcast') {
        this.log(timestamp + ` ` + username.toString ` : ` + chalk.cyan(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command.charAt(0) === '@') {
        this.log(timestamp + ` ` + username.toString ` : ` + chalk.white(Message.fromJSON(buffer).toString()))
      } else if (Message.fromJSON(buffer).command === 'users') {
        this.log(timestamp + ` ` + username.toString ` : ` + chalk.magenta(Message.fromJSON(buffer).toString()))
      }
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /[^,\s:]+/g)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'broadcast') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command.charAt(0) === '@') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'users') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else {
      this.log(`Command <${command}> was not recognized.  Please enter usable Command`)
    }

    callback()
  })
