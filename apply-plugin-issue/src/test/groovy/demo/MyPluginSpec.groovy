package demo

import nebula.test.IntegrationSpec

class MyPluginSpec extends IntegrationSpec {

    def 'mytest'() {
        given:
        buildFile << """\
            ${applyPlugin(MyPlugin)}

            group = 'nebula.test.example'

            """.stripIndent()

        when:
        def result = runTasks('bE')

        then:
        result.success
    }
}
