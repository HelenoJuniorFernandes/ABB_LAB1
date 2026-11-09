import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

    /** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente no vetor */
    static int quantosProdutos = 0;

    static ABB<String, Produto> produtosCadastradosPorNome;
    
    static ABB<Integer, Produto> produtosCadastradosPorId;
    
    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }
    
    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
        
        T valor;
        
        System.out.println(mensagem);
        try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }
    
    /** 
     * Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * @return Um inteiro com a opção do usuário.
    */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar produto, por nome");
        System.out.println("3 - Procurar produto, por id");
        System.out.println("4 - Remover produto, por nome");
        System.out.println("5 - Remover produto, por id");
        System.out.println("6 - Recortar a lista de produtos, por nome");
        System.out.println("7 - Recortar a lista de produtos, por id");
        System.out.println("0 - Finalizar");
        
        return lerOpcao("Digite sua opção: ", Integer.class);
    }
    
    /**
     * Lê os dados de um arquivo-texto e retorna uma ávore de produtos. Arquivo-texto no formato
     * N  (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna uma árvore vazia em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Uma árvore com os produtos carregados, ou vazia em caso de problemas de leitura.
     */
    static <K> ABB<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
        
        Scanner arquivo = null;
        int numProdutos;
        String linha;
        Produto produto;
        ABB<K, Produto> produtosCadastrados;
        
        try {
            arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
            
            numProdutos = Integer.parseInt(arquivo.nextLine());
            produtosCadastrados = new ABB<K, Produto>();
            
            for (int i = 0; i < numProdutos; i++) {
                linha = arquivo.nextLine();
                produto = Produto.criarDoTexto(linha);
                K chave = extratorDeChave.apply(produto);
                produtosCadastrados.inserir(chave, produto);
            }
            quantosProdutos = numProdutos;
            
        } catch (IOException excecaoArquivo) {
            produtosCadastrados = null;
        } finally {
            if(arquivo != null) arquivo.close();
        }
        
        return produtosCadastrados;
    }
    
    private static void mostrarProduto(Produto produto) {
        cabecalho();
        StringBuilder mensagem = new StringBuilder("Produto não encontrado.\n");
        
        if (produto != null) {
            mensagem = new StringBuilder(String.format("%s\n", produto));            
        }

        System.out.println(mensagem.toString());
    }

    /** Lista todos os produtos cadastrados, numerados, um por linha */
    static <K> void listarTodosOsProdutos(ABB<K, Produto> produtosCadastrados) {
        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        System.out.println(produtosCadastrados.percorrer());
    }

    // ==========================================
    // TAREFAS 4 E 5: Métodos Genéricos 
    // ==========================================
    static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K procurado) {
        Produto encontrado = produtosCadastrados.pesquisar(procurado);
        
        System.out.println("\n--- Medições de Desempenho ---");
        System.out.println("Comparações realizadas: " + produtosCadastrados.getComparacoes());
        System.out.println("Tempo de execução (ms): " + produtosCadastrados.getTempo());
        System.out.println("------------------------------");
        
        return encontrado;
    }

    static <K> Produto removerProduto(ABB<K, Produto> produtosCadastrados, K chave){
        try {
            return produtosCadastrados.remover(chave);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // ==========================================
    // MÉTODOS DE BUSCA E REMOÇÃO DO MENU (Opções 2 a 5)
    // ==========================================
    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        Integer id = lerOpcao("Digite o ID do produto a procurar:", Integer.class);
        return localizarProduto(produtosCadastrados, id);
    }
    
    static Produto localizarProdutoNome(ABB<String, Produto> produtosCadastrados) {
        System.out.println("Digite o nome do produto a procurar:");
        String nome = teclado.nextLine();
        return localizarProduto(produtosCadastrados, nome);
    }
    
    static Produto removerProdutoId(ABB<Integer, Produto> produtosCadastrados) {
        Integer id = lerOpcao("Digite o ID do produto a remover:", Integer.class);
        return removerProduto(produtosCadastrados, id);
    }

    static Produto removerProdutoNome(ABB<String, Produto> produtosCadastrados) {
        System.out.println("Digite o nome do produto a remover:");
        String nome = teclado.nextLine();
        return removerProduto(produtosCadastrados, nome);
    }

    // ==========================================
    // TAREFA 3: Métodos de Recorte (Menu opções 6 e 7)
    // ==========================================
    private static <K> void recortarProduto(ABB<K, Produto> produtosCadastrados, K deOnde, K ateOnde) {
        System.out.println("\n--- Recorte de Produtos ---");
        System.out.println("Filtrando de: " + deOnde + " até " + ateOnde);
        
        Lista<Produto> resultado = produtosCadastrados.recortar(deOnde, ateOnde);
        
        if (resultado.vazia()) {
            System.out.println("Nenhum produto encontrado neste intervalo.");
        } else {
            System.out.println("Produtos encontrados no recorte:");
            System.out.println(resultado.toString());
        }
    }
    
    private static void recortarProdutosNome(ABB<String, Produto> produtosCadastrados) {
        System.out.println("Digite o nome inicial do recorte:");
        String inicio = teclado.nextLine();
        System.out.println("Digite o nome final do recorte:");
        String fim = teclado.nextLine();
        recortarProduto(produtosCadastrados, inicio, fim);
    }
     
    private static void recortarProdutosId(ABB<Integer, Produto> produtosCadastrados) {
        Integer inicio = lerOpcao("Digite o ID inicial do recorte:", Integer.class);
        Integer fim = lerOpcao("Digite o ID final do recorte:", Integer.class);
        recortarProduto(produtosCadastrados, inicio, fim);
    }
    
    public static void main(String[] args) {
        teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        
        produtosCadastradosPorNome = lerProdutos(nomeArquivoDados, (p -> p.descricao));
        
        // Só carrega a árvore por ID se o ficheiro foi lido corretamente
        if (produtosCadastradosPorNome != null) {
            produtosCadastradosPorId = new ABB<Integer, Produto>((id1, id2) -> id1.compareTo(id2));
            
            // Lemos novamente do ficheiro para popular a árvore de ID com o extrator apropriado
            produtosCadastradosPorId = lerProdutos(nomeArquivoDados, (p -> p.idProduto));
        }
        
        int opcao = -1;
      
        do {
            // Tratamento caso lerOpcao retorne null (usuário digitou letra onde era número)
            Integer lido = menu();
            opcao = (lido != null) ? lido : -1;
            
            switch (opcao) {
            case 1 -> listarTodosOsProdutos(produtosCadastradosPorNome);
            case 2 -> mostrarProduto(localizarProdutoNome(produtosCadastradosPorNome));
            case 3 -> mostrarProduto(localizarProdutoID(produtosCadastradosPorId));
            case 4 -> mostrarProduto(removerProdutoNome(produtosCadastradosPorNome));
            case 5 -> mostrarProduto(removerProdutoId(produtosCadastradosPorId));
            case 6 -> recortarProdutosNome(produtosCadastradosPorNome); 
            case 7 -> recortarProdutosId(produtosCadastradosPorId); 
            case 0 -> System.out.println("FLW VLW OBG VLT SMP.");
            default -> {
                if (opcao != -1) System.out.println("Opção inválida.");
            }
            }
            if (opcao != 0) pausa();
            
        } while (opcao != 0);       

        teclado.close();    
    }
}