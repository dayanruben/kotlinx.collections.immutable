/*
 * Copyright 2016-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package generators.immutableListBuilder

import generators.BenchmarkSourceGenerator
import java.io.PrintWriter

interface ListBuilderAddBenchmark {
    val packageName: String
    fun listBuilderType(T: String): String
    val getOperation: String
}

class ListBuilderAddBenchmarkGenerator(private val impl: ListBuilderAddBenchmark) : BenchmarkSourceGenerator() {
    override val outputFileName: String = "Add"

    override fun getPackage(): String {
        return super.getPackage() + ".immutableList." + impl.packageName
    }

    override val imports: Set<String> = super.imports + "org.openjdk.jmh.infra.Blackhole"

    override fun generateBody(out: PrintWriter) {
        out.println("""
open class Add {
    @Param("10000", "100000")
    var size: Int = 0

    @Param("0.0", "50.0")
    var immutablePercentage: Double = 0.0

    @Benchmark
    fun addLast(): ${impl.listBuilderType("String")} {
        return persistentListBuilderAdd(size, immutablePercentage)
    }

    @Benchmark
    fun addLastAndGet(bh: Blackhole) {
        val builder = persistentListBuilderAdd(size, immutablePercentage)
        for (i in 0 until size) {
            bh.consume(builder.${impl.getOperation}(i))
        }
    }
        """.trimIndent())
        if (impl is ListBuilderIterateBenchmark) {
            out.println("""
    @Benchmark
    fun addLastAndIterate(bh: Blackhole) {
        val builder = persistentListBuilderAdd(size, immutablePercentage)
        for (e in builder) {
            bh.consume(e)
        }
    }
}
            """.trimIndent()
            )
        } else {
            out.println("}")
        }
    }
}