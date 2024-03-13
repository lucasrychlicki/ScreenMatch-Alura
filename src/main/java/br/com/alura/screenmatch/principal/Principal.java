package br.com.alura.screenmatch.principal;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=f7a10efb";

    public void exibeMenu(){
        System.out.println("Digite o nome da série para busca: ");
        var nomeSerie = leitura.nextLine();
		var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();
		
		for (int i = 1; i<=dados.totalTemporadas(); i++){
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

        // for(int i = 0; i < dados.totalTemporadas(); i++){
        //     List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
        //     for(int j = 0; j < episodiosTemporada.size(); j++){
        //         System.out.println(episodiosTemporada.get(j).titulo());
        //     }
        // }

        /* //t = temporadas & e = episodios
        //"->" = lambda
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo()))); 

        List<String> nomes = Arrays.asList("Lucas", "Murilo", "Gustavo", "Igor", "Pedro");

        nomes.stream()
                .sorted()
                .limit(3)
                .filter(n -> n.startsWith("L"))
                .map(n -> n.toLowerCase())
                .forEach(System.out::println);  */

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList()); 
        
        // Buscar o top5 de episódios pela avaliação
        // System.out.println("\nTop 5 episódios: ");
        // dadosEpisodios.stream()
        //         .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
        //         .peek(e -> System.out.println("Primeiro Filtro(N/A) " + e))
        //         .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
        //         .peek(e -> System.out.println("Ordenação " + e))
        //         .limit(10)
        //         .peek(e -> System.out.println("Limite " + e))
        //         .map(e -> e.titulo().toUpperCase())
        //         .peek(e -> System.out.println("Mapeamento " + e))
        //         .forEach(System.out::println);
        
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                    .map(d -> new Episodio(t.numero(), d)))
                .collect(Collectors.toList());

        episodios.forEach(System.out::println);
        
        // Buscar episódio a partir do título dele
        // System.out.println("Digite o trecho do título do episódio para busca: ");
        // var trechoTitulo = leitura.nextLine();
        // Optional<Episodio> episodioBuscado = episodios.stream()
        //         .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
        //         .findFirst();
        // if(episodioBuscado.isPresent()){
        //         System.out.println("Episódio encontrado!");
        //         System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
        // } else {
        //         System.out.println("Episódio não encontrado!");
        // }

        // Buscar todos os episódios a partir do ano que o usuário deseja
        // System.out.println("A partir de que ano você deseja ver os episódios? ");
        // var ano = leitura.nextInt();
        // leitura.nextLine();

        // LocalDate dataBusca = LocalDate.of(ano, 1, 1);

        // DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        // episodios.stream()
        //         .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
        //         .forEach(e -> System.out.println(
        //                 "Temporada: " + e.getTemporada() +
        //                 " Episódio: " + e.getTitulo() +
        //                 " Data de lançamento: " + e.getDataLancamento().format(formatador)
        //         ));

        // Método para pegar a média das avaliações das temporadas
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada, 
                Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        
        // Pegar as estatisticas dos episódios
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Média: " + est.getAverage());
        System.out.println("Pior avaliado: " + est.getMin());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Quantidade: " + est.getCount());
    }
}
