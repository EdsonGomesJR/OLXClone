package com.edson.olxclone.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.edson.olxclone.R;
import com.edson.olxclone.helper.ConfiguracaoFirebase;
import com.edson.olxclone.helper.Permissoes;
import com.edson.olxclone.model.Anuncio;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskedittext.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CadastrarAnuncioActivity extends AppCompatActivity
        implements View.OnClickListener {

    private EditText campoTitulo, campoDescricao;
    private ImageView imagem1, imagem2, imagem3;
    private CurrencyEditText campoValor;
    private MaskEditText campoTelefone;
    private Spinner campoEstado, campoCategoria;
    private Button buttonCadastrarAnuncio;
    private Anuncio anuncio;

    private StorageReference storage;

    private String[] permissoes = new String[]{

            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private List<String> listaFotosRecuperadas = new ArrayList<>();
    private List<String> listaUrlFotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);

        //config iniciais
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        //validar permissoes
        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();
        carregarDadosSpinner();
    }


    private void inicializarComponentes() {

        campoTitulo = findViewById(R.id.editTitulo);
        campoDescricao = findViewById(R.id.editDescricao);
        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        campoEstado = findViewById(R.id.spinnerEstados);
        campoCategoria = findViewById(R.id.spinnerCategoria);
        buttonCadastrarAnuncio = findViewById(R.id.buttonCadastrarAnuncio);


        imagem1 = findViewById(R.id.imageCadastro1);
        imagem2 = findViewById(R.id.imageCadastro2);
        imagem3 = findViewById(R.id.imageCadastro3);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);

        //config localidade para pt-BR
        Locale locale = new Locale("pt", "BR");
        campoValor.setLocale(locale);

    }

    private void carregarDadosSpinner() {
        //spinner adapter
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoEstado.setAdapter(adapter);

        String[] categorias = getResources().getStringArray(R.array.categoria);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categorias);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoCategoria.setAdapter(adapterCategoria);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {

            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();


            }
        }
    }

    private void alertaValidacaoPermissao() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessario aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void exibirMensagemErro(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    public void salvarAnuncio() {

        /**
         * salvar imagem no storage
         */
        for(int i = 0; i < listaFotosRecuperadas.size(); i++){
            String urlImagem  = listaFotosRecuperadas.get(i);
            int tamanhoLista = listaFotosRecuperadas.size();
            salvarFotoStorage(urlImagem, tamanhoLista, i);
        }

    }

    private void salvarFotoStorage(String urlStringCaminhoFoto, final int totalFotos, int contador){

        //criar nó no storage
        StorageReference imagemAnuncio = storage.child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem"+contador);

        //faz upload do arquivo

        UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(urlStringCaminhoFoto));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Uri firebaseUrl = taskSnapshot.getUploadSessionUri();
                String urlConvertida = firebaseUrl.toString();
                listaUrlFotos.add(urlConvertida);

                if(totalFotos == listaUrlFotos.size()){

                    anuncio.setFotos(listaUrlFotos);
                    anuncio.salvar();
                    Toast.makeText(CadastrarAnuncioActivity.this, "Produto Cadastrado!!", Toast.LENGTH_SHORT).show();
                    finish();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                exibirMensagemErro("falha ao fazer upload");
                Log.i("INFO", "Falha ao fazer upload: " + e.getMessage());

            }
        });





    }

    private Anuncio configurarAnuncio(){

        String fone = "";
        String estado = campoEstado.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTitulo.getText().toString();
        String valor = campoValor.getText().toString();
        String telefone = campoTelefone.getText().toString();
        if (campoTelefone.getRawText() != null) {
            fone = campoTelefone.getRawText();
        }
        String descricao = campoDescricao.getText().toString();



      Anuncio  anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);

        return anuncio;
    }

    public void validarDadosAnuncio(  ) {

        anuncio = configurarAnuncio();
        String valor = String.valueOf(campoValor.getRawValue());


        String fone = "";
        if(campoTelefone.getRawText() != null){
            fone = campoTelefone.getRawText();
        }


        if (listaFotosRecuperadas.size() != 0) {
            if (!anuncio.getEstado().isEmpty()) {
                if (!anuncio.getCategoria().isEmpty()) {
                    if (!anuncio.getTitulo().isEmpty()) {
                        if (!valor.isEmpty() && !valor.equals("0")) {
                            if (!anuncio.getTelefone().isEmpty() && fone.length() >= 10) {
                                if (!anuncio.getDescricao().isEmpty()) {

                                    salvarAnuncio();

                                } else {
                                    exibirMensagemErro("Preencha o campo Descrição");


                                }
                            } else {
                                exibirMensagemErro("Preencha o campo Telefone, Digite ao menos 10 números");


                            }
                        } else {
                            exibirMensagemErro("Preencha o campo Valor");


                        }

                    } else {
                        exibirMensagemErro("Preencha o campo TITULO");

                    }
                } else {
                    exibirMensagemErro("Preencha o campo CATEGORIA");

                }
            } else {
                exibirMensagemErro("Preencha o campo Estado");

            }

        } else {
            exibirMensagemErro("Selecione ao menos uma foto");

        }


    }



    @Override
    public void onClick(View v) {
//onclick para selecionar as imagens
        switch (v.getId()) {
            case R.id.buttonCadastrarAnuncio:
                validarDadosAnuncio();
                break;
            case R.id.imageCadastro1:
                escolherImagem(1);
                break;
            case R.id.imageCadastro2:
                escolherImagem(2);
                break;
            case R.id.imageCadastro3:
                escolherImagem(3);
                break;



        }
    }

    public void escolherImagem(int requestCode) {

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            //recuperar imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //configura imagem no imageview
            if (requestCode == 1) {
                imagem1.setImageURI(imagemSelecionada);

            } else if (requestCode == 2) {

                imagem2.setImageURI(imagemSelecionada);

            } else if (requestCode == 3) {

                imagem3.setImageURI(imagemSelecionada);
            }
            listaFotosRecuperadas.add(caminhoImagem);

        }
    }
}
